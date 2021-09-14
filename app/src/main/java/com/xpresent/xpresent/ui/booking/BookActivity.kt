/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 06.07.20 11:22
 */
package com.xpresent.xpresent.ui.booking

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wallet.WalletConstants
import com.google.firebase.analytics.FirebaseAnalytics
import com.xpresent.xpresent.R
import com.xpresent.xpresent.config.config
import com.xpresent.xpresent.requests.ServerConnector
import com.xpresent.xpresent.requests.ServerConnector.AsyncResponse
import org.json.JSONException
import org.json.JSONObject
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.localization.AsdkSource
import ru.tinkoff.acquiring.sdk.localization.Language

import ru.tinkoff.acquiring.sdk.models.AsdkState
import ru.tinkoff.acquiring.sdk.models.GooglePayParams
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.payment.PaymentListener
import ru.tinkoff.acquiring.sdk.payment.PaymentListenerAdapter
import ru.tinkoff.acquiring.sdk.payment.PaymentProcess
import ru.tinkoff.acquiring.sdk.utils.GooglePayHelper
import ru.tinkoff.acquiring.sdk.utils.Money
import kotlin.math.min
import kotlin.math.roundToInt

class BookActivity : AppCompatActivity() {
    private var settings: SharedPreferences? = null
    private var buttonBook: Button? = null
    lateinit var tinkoffAcquiring: TinkoffAcquiring
    private var googlePayHelper: GooglePayHelper? = null
    private val paymentListener = createPaymentListener()
    private lateinit var progressDialog: AlertDialog
    var paymentProcess: PaymentProcess? = null
    private var errorDialog: AlertDialog? = null
    private var isProgressShowing = false
    private var isErrorShowing = false

    // get sessionKey and order params from storage
    private var sessionKey: String? = null

    // order info
    private var ordId: Int = 0
    private var cityId: Int = 0
    private var offerId: String = ""
    private var orderSum: Int = 0
    private var orderOldSum: Int = 0
    private var cashBack: Int = 0
    private var availableCashBack: Int = 0
    private var impressionName: String = ""
    private var human_name: String = ""
    private var duration_name: String = ""
    private var selectedDate: String = ""
    private var selectedDateFormat: String = ""
    private var selectedTime: String = ""
    private var cashBackCharged: Boolean = false // if cashback was applied to the order

    // client info
    private var clientPhone: String = ""
    private var clientEmail: String = ""
    private var clientName: String = ""
    private val REQUEST_CODE_PAYMENT: Int = 1 // payment status
    private val GOOGLE_PAY_REQUEST_CODE: Int = 5 // google pay status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        tinkoffAcquiring = TinkoffAcquiring(config.TINKOFF_TERMINAL_KEY, config.TINKOFF_PASSWORD, config.TINKOFF_PUBLIC_KEY)
        AcquiringSdk.isDeveloperMode = false // используется тестовый URL, деньги с карт не списываются
        AcquiringSdk.isDebug = true         // включение логирования запросов

        settings =  getSharedPreferences("xp_client", Context.MODE_PRIVATE)
        sessionKey = settings?.getString("sessionKey", "")
        buttonBook = findViewById(R.id.btnBook)

        initDialogs()
        // get client cash back from server
        getCashBack()
        // use cashback
        val cashBackCheck = findViewById<CheckBox>(R.id.cashbackCheck)
        cashBackCheck.setOnClickListener { v -> setCashBack(v) }
        // get order params
        getOrderParams()
        // set cart price and etc
        setOrderParams()
        // set up google pay button
        setupGooglePay()
        // proceed to payment
        val buttonBook = findViewById<Button>(R.id.btnBook)
        buttonBook.setOnClickListener { onClickBook(REQUEST_CODE_PAYMENT) }
        // back button
        val backBtn = findViewById<ImageView>(R.id.iv_nav)
        backBtn.setOnClickListener { onBackPressed() }

    }

    private fun setupGooglePay() {
        val googlePayButton = findViewById<View>(R.id.google_pay) // определяем кнопку, вставленную в разметку

        val googleParams = GooglePayParams(config.TINKOFF_TERMINAL_KEY,     // конфигурируем основные параметры
                environment = WalletConstants.ENVIRONMENT_PRODUCTION//WalletConstants.ENVIRONMENT_PRODUCTION // тестовое окружение
        )

        googlePayHelper = GooglePayHelper(googleParams) // передаем параметры в класс-помощник

        googlePayHelper!!.initGooglePay(this) { ready ->      // вызываем метод для определения доступности Google Pay на девайсе
            if (ready) {                                    // если Google Pay доступен и настроен правильно, по клику на кнопку открываем экран оплаты Google Pay
                googlePayButton.setOnClickListener {
                    onClickBook(GOOGLE_PAY_REQUEST_CODE)
                }
            } else {
                googlePayButton.visibility = View.GONE      // если Google Pay недоступен на девайсе, необходимо скрыть кнопку
            }
        }
    }

    private fun onClickBook(type:Int) {
        buttonBook = findViewById(R.id.btnBook)
        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        clientName = etName.text.toString()
        clientPhone = etPhone.text.toString()
        clientEmail = etEmail.text.toString()
        if (clientName.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.enter_name), Toast.LENGTH_LONG).show()
            return
        }
        if (clientPhone.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.enter_phone), Toast.LENGTH_LONG).show()
            return
        }
        if (clientEmail.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.enter_email), Toast.LENGTH_LONG).show()
            return
        }
        if(type == REQUEST_CODE_PAYMENT)
            buttonBook!!.isEnabled = false
        // record client info to storage
        val editor = settings!!.edit()
        editor.putString("clientEmail", clientEmail)
        editor.putString("clientName", clientName)
        // if cashback was applied to the order
        if (cashBackCharged) {
            editor.remove("cashback") // remove old cashback
            editor.apply()
            cashBack -= availableCashBack
            editor.putInt("cashback", cashBack)
            editor.apply()
        }
        val context: Context = this
        val mapPost = HashMap<String, String>()

        mapPost["action"] = "order"
        mapPost["password_app"] = config.PASSWORD_APP
        // client
        mapPost["session_key"] = sessionKey!!
        mapPost["phone"] = clientPhone
        mapPost["email"] = clientEmail
        mapPost["name"] = clientName
        // order
        mapPost["type"] = "impression"
        mapPost["city_id"] = cityId.toString()
        mapPost["payment_id"] = "2" // bank card
        mapPost["delivery_id"] = "3" // electronic
        mapPost["closed"] = "1" // activate certificate
        mapPost["item_id"] = offerId
        mapPost["sum"] = orderSum.toString()
        mapPost["item_name"] = impressionName
        mapPost["utm"] = "android"
        mapPost["used_cashback"] = availableCashBack.toString()
        // if booking
        mapPost["selected_date"] = "$selectedDate $selectedTime"

        val connector = ServerConnector(context, AsyncResponse { success,
                                                                 output -> if (success) doPayment(output, type)
                                                                            else Toast.makeText(context, output, Toast.LENGTH_LONG).show() }, true)
        connector.execute(mapPost)
    }

    private fun doPayment(output: String, type: Int) {
        try {
            val jsonResult = JSONObject(output)
            val status = jsonResult.getBoolean("status")
            if (status) {
                ordId = jsonResult.getInt("order_id")
                if (cashBackCharged) {
                    cashBack -= availableCashBack
                }
                when(type) {
                    REQUEST_CODE_PAYMENT -> {
                        tinkoffAcquiring.openPaymentScreen(this, createPaymentOptions(), REQUEST_CODE_PAYMENT)
                    }
                    GOOGLE_PAY_REQUEST_CODE -> {
                        googlePayHelper!!.openGooglePay(this@BookActivity, Money.ofRubles(orderSum.toLong()), GOOGLE_PAY_REQUEST_CODE)
                    }
                }
            }
            else{
                buttonBook!!.isEnabled = true
                Toast.makeText(this, jsonResult.getString("error"), Toast.LENGTH_LONG).show()
            }
        } catch (e: JSONException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun createPaymentOptions(): PaymentOptions {
        return PaymentOptions().setOptions {
                    orderOptions {                          // данные заказа
                        orderId = ordId.toString()                // ID заказа в вашей системе
                        amount = Money.ofRubles(orderSum.toLong())       // сумма для оплаты
                        title = resources.getString(R.string.payment_coment)+orderId          // название платежа, видимое пользователю
                        description = "$impressionName ($human_name, $duration_name)"    // описание платежа, видимое пользователю
                        recurrentPayment = false            // флаг определяющий является ли платеж рекуррентным [1]
                    }
                    customerOptions {                       // данные покупателя
                        checkType = CheckType.NO.toString() // тип привязки карты
                        customerKey = clientPhone        // уникальный ID пользователя для сохранения данных его карты
                        email = clientEmail          // E-mail клиента для отправки уведомления об оплате
                    }
                    featuresOptions {                       // настройки визуального отображения и функций экрана оплаты
                        useSecureKeyboard = true            // флаг использования безопасной клавиатуры [2]
                        cameraCardScanner = null
                        localizationSource = AsdkSource(Language.RU)
                        theme = R.style.AcquiringTheme
                    }
                }
    }

    private fun createPaymentListener(): PaymentListener {
        return object : PaymentListenerAdapter() {

            override fun onSuccess(paymentId: Long, cardId: String?) {
                hideProgressDialog()
                successActivity()
            }

            override fun onUiNeeded(state: AsdkState) {
                hideProgressDialog()
                tinkoffAcquiring.openPaymentScreen(
                        this@BookActivity,
                        createPaymentOptions(),
                        REQUEST_CODE_PAYMENT,
                        state)
            }

            override fun onError(throwable: Throwable) {
                hideProgressDialog()
                showErrorDialog()
                paymentProcess = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        paymentProcess?.unsubscribe()
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        if (errorDialog != null && errorDialog!!.isShowing) {
            errorDialog!!.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_PAYMENT -> {
                buttonBook!!.isEnabled = true
                handlePaymentResult(resultCode)
            }
            GOOGLE_PAY_REQUEST_CODE -> {
                handleGooglePayResult(resultCode, data)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handlePaymentResult(resultCode: Int) {
        when (resultCode) {
            RESULT_OK -> successActivity()
            RESULT_CANCELED -> {}
            TinkoffAcquiring.RESULT_ERROR -> Toast.makeText(this, R.string.payment_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGooglePayResult(resultCode: Int, data: Intent?) {
        if (data != null && resultCode == Activity.RESULT_OK) {
            val token = GooglePayHelper.getGooglePayToken(data)
            if (token == null) {
                showErrorDialog()
            } else {
                showProgressDialog()
                paymentProcess = tinkoffAcquiring
                        .initPayment(token, createPaymentOptions())
                        .subscribe(paymentListener)
                        .start()
            }
        } else if (resultCode != Activity.RESULT_CANCELED) {
            showErrorDialog()
        }
    }

    private fun successActivity(){
        val extras = Bundle()
        extras.putInt("paymentId", 2)
        extras.putInt("orderId", ordId)

        // Purchase in Firebase Analytics
        var mFirebaseAnalytics: FirebaseAnalytics? = null
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, orderSum.toString())
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "RUB")
        bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, ordId.toString())
        /*val item1 = Bundle()
        item1.putString(FirebaseAnalytics.Param.ITEM_NAME, "Полет на самолете")
        item1.putString(FirebaseAnalytics.Param.ITEM_ID, "599")
        item1.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Полеты")
        bundle.putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(item1))*/
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)

        val intent = Intent(this, PaymentResultActivity::class.java)
        intent.putExtras(extras)
        startActivity(intent)
    }

    private fun showErrorDialog() {
        errorDialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.error_title)
            setMessage(getString(R.string.error_message))
            setNeutralButton("OK") { dialog, _ ->
                dialog.dismiss()
                isErrorShowing = false
            }
        }.show()
        isErrorShowing = true
    }

    private fun initDialogs() {
        progressDialog = AlertDialog.Builder(this).apply {
            setCancelable(false)
            setView(layoutInflater.inflate(R.layout.dialog_loading, null))
        }.create()

        if (isProgressShowing) {
            showProgressDialog()
        }
        if (isErrorShowing) {
            showErrorDialog()
        }
    }

    private fun showProgressDialog() {
        progressDialog.show()
        isProgressShowing = true
    }

    private fun hideProgressDialog() {
        progressDialog.dismiss()
        isProgressShowing = false
    }


    private fun getCashBack() {
        val mapPost: HashMap<String, String> = HashMap()
        mapPost["action"] = "cashback"
        mapPost["session_key"] = sessionKey!!
        val Connector = ServerConnector(this, ServerConnector.AsyncResponse { success, output ->
            if (success) {
                try {
                    val jsonResult = JSONObject(output)
                    val status = jsonResult.getBoolean("status")
                    if (status) {
                        val cashBackTxt = findViewById<TextView>(R.id.total_cashback)
                        val cashbackCheck = findViewById<CheckBox>(R.id.cashbackCheck)
                        cashBack = jsonResult.getInt("cashback")
                        //val cash = cashBack.toDouble();
                        /* no limit cashback */
                        //availableCashBack = min(cashBack, orderSum - 1)
                        /* limit cashback */
                        availableCashBack = Math.min(cashBack, (orderSum * config.MAX_CASHBACK_PERCENT).toInt())
                        val cashBackStr = resources.getString(R.string.cashback_you_have) + " " + cashBack + " " + config.RUB
                        cashBackTxt.text = cashBackStr
                        if (availableCashBack != 0) {
                            val useCashBackStr = (resources.getString(R.string.cashback_use) + " " + availableCashBack + " " + config.RUB + " "
                                    + resources.getString(R.string.cashback_use2)) /* + " " + config.MAX_CASHBACK_PERCENT +"%)";*/
                            cashbackCheck.text = useCashBackStr
                        } else {
                            val useCashBackLayout = findViewById<RelativeLayout>(R.id.useCashBackLayout)
                            useCashBackLayout.visibility = View.GONE
                        }
                    }
                } catch (e: JSONException) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }, false)
        Connector.execute(mapPost)
    }

    private fun setCashBack(v: View) {
        var usedCashBack = ""
        if ((v as CheckBox).isChecked) {
            orderSum -= availableCashBack
            usedCashBack = "-" + availableCashBack + " " + config.RUB
            cashBackCharged = true
        } else {
            orderSum += availableCashBack
            cashBackCharged = false
        }
        val sum = orderSum.toString() + " " + config.RUB
        val totalPriceTxt = findViewById<TextView>(R.id.total_price)
        val usedCashBackTxt = findViewById<TextView>(R.id.used_cashback)
        val earnCashBackTxt = findViewById<TextView>(R.id.earnCashBack)
        val earnCashBack = (orderSum * config.CASHBACK).roundToInt()
        val earnCashBackStr = "<font color='" + resources.getColor(R.color.colorAccent) + "'>+" + earnCashBack + " " + config.RUB + "</font> " + resources.getString(R.string.get_bonus2)
        totalPriceTxt.text = sum
        usedCashBackTxt.text = usedCashBack
        earnCashBackTxt.text = Html.fromHtml(earnCashBackStr)
    }

    private fun getOrderParams() {
        offerId = settings!!.getString("itemId", "").toString()
        cityId = settings!!.getInt("cityId", 1)
        orderSum = settings!!.getInt("orderSum", 0)
        orderOldSum = settings!!.getInt("price_old", 0)
        selectedDate = settings!!.getString("selectedDate", "")!!
        selectedDateFormat = settings!!.getString("selectedDateFormat", "")!!
        selectedTime = settings!!.getString("selectedTime", "")!!
        impressionName = settings!!.getString("itemName", "")!!
        clientPhone = settings!!.getString("clientPhone", "")!!
        clientEmail = settings!!.getString("clientEmail", "")!!
        clientName = settings!!.getString("clientName", "")!!
        human_name = settings!!.getString("human_name", "")!!
        duration_name = settings!!.getString("duration_name", "")!!
    }

    private fun setOrderParams() {
        val impOfferNameTxt = findViewById<TextView>(R.id.impressionOfferName)
        val durationNameTxt = findViewById<TextView>(R.id.duration_name)
        val humanNameTxt = findViewById<TextView>(R.id.human_name)
        val impSumTxt = findViewById<TextView>(R.id.impSum)
        val totalPriceTxt = findViewById<TextView>(R.id.total_price)
        val dateTimeTxt = findViewById<TextView>(R.id.datetime)
        val priceTxt = findViewById<TextView>(R.id.price)
        val priceOldTxt = findViewById<TextView>(R.id.oldPrice)
        val infoPriceOldTxt = findViewById<TextView>(R.id.info_price_old)
        val earnCashBackTxt = findViewById<TextView>(R.id.earnCashBack)
        if (clientPhone.isNotEmpty()) {
            val etPhone = findViewById<EditText>(R.id.etPhone)
            etPhone.setText(clientPhone)
        }
        if (clientEmail.isNotEmpty()) {
            val etEmail = findViewById<EditText>(R.id.etEmail)
            etEmail.setText(clientEmail)
        }
        if (clientName.isNotEmpty()) {
            val etName = findViewById<EditText>(R.id.etName)
            etName.setText(clientName)
        }
        val sum = orderSum.toString() + " " + config.RUB
        val oldSum = orderOldSum.toString() + " " + config.RUB
        val earnCashBack = (orderSum * config.CASHBACK).roundToInt()
        val earnCashBackStr = "<font color='" + resources.getColor(R.color.colorAccent) + "'>+" + earnCashBack + " " + config.RUB + "</font> " + resources.getString(R.string.get_bonus2)
        val dateTimeStr = "$selectedDateFormat в $selectedTime"
        impOfferNameTxt.text = impressionName
        dateTimeTxt.text = dateTimeStr
        durationNameTxt.text = duration_name
        humanNameTxt.text = human_name
        impSumTxt.text = sum
        if (orderOldSum == 0) {
            priceOldTxt.visibility = View.GONE
            val priceOldText = findViewById<TextView>(R.id.priceOldTxt)
            priceOldText.visibility = View.GONE
        } else {
            priceOldTxt.text = oldSum
            infoPriceOldTxt.text = oldSum
        }
        priceTxt.text = sum
        totalPriceTxt.text = sum
        earnCashBackTxt.text = Html.fromHtml(earnCashBackStr)
    }
}