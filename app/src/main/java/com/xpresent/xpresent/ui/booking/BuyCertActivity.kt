/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 06.07.20 11:22
 */
package com.xpresent.xpresent.ui.booking

import android.annotation.SuppressLint
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
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemChangeListener
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.gms.wallet.WalletConstants
import com.google.android.material.chip.Chip
import com.google.firebase.analytics.FirebaseAnalytics
import com.xpresent.xpresent.R
import com.xpresent.xpresent.config.config
import com.xpresent.xpresent.custom_view.SelectorButton
import com.xpresent.xpresent.requests.ServerConnector
import com.xpresent.xpresent.ui.catalog.FullImageActivity
import com.xpresent.xpresent.util.dpToPx
import com.xpresent.xpresent.util.drawablesBottom
import com.xpresent.xpresent.util.paddingTop
import com.xpresent.xpresent.util.string
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
import kotlin.math.roundToInt


class BuyCertActivity : AppCompatActivity() {
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
    private var ordType: String? = "" // набор или впечатление
    private var itemId: String? = "" // ID впечатления или набора
    private var ordId: Int = 0
    private var cityId: Int = 1
    private var orderSum: Int = 0
    private var orderOldSum: Int = 0
    private var cashBack: Int = 0
    private var availableCashBack: Int = 0
    private var itemName: String = ""
    private var human_name: String = ""
    private var duration_name: String = ""
    private var cashBackCharged: Boolean = false // применили ли кешбек к заказу
    private var deliveryOrderPrice: Int = 0 // стоимость доставки в заказе

    // delivery info
    private var tabDelEmail: Chip? = null
    private var tabDelCourier: Chip? = null
    private var tabDelPoint: Chip? = null
    private lateinit var selectorButton: SelectorButton
    private lateinit var toWhomLayout: LinearLayout
    private lateinit var etRecipientEmailText: EditText
    private lateinit var hintRecipientEmailText: TextView
    private lateinit var etCommentsText: EditText
    private var deliveryId: Int = 3 // 3 - электронный, 1 - курьером, 2 - самовывоз
    private var viewId: Int = 3 // вид шаблона сертификата
    private var totalPrice: Int = 0 // вид шаблона сертификата
    private var orderPricePay: Int = 0 // сумма заказа для бесплатной доставки
    private var deliveryPrice: Int = 0 // стоимость курьерской доставки
    private var orderPricePickupPay: Int = 0 // сумма заказа для бесплатной доставки
    private var pickupPrice: Int = 0 // стоимость курьерской доставки
    private var pickupPriceDelivery = ArrayList<Int>() // стоимость доставки в каждом пункте выдачи

    // payment info
    private var paymentId: Int = 2 // тип оплаты
    private val REQUEST_CODE_PAYMENT: Int = 2 // оплата картой
    private val GOOGLE_PAY_REQUEST_CODE: Int = 4 // Google Pay
    private val CASH_CODE_PAYMENT: Int = 1 // оплата наличными

    // client info
    private var clientPhone: String = ""
    private var clientEmail: String = ""
    private var clientName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_cert)

        selectorButton = findViewById(R.id.selectorButton)
        toWhomLayout = findViewById(R.id.toWhomLayout)
        etRecipientEmailText = findViewById(R.id.etRecipientEmailText)
        hintRecipientEmailText = findViewById(R.id.hintRecipientEmailText)
        etCommentsText = findViewById(R.id.etCommentsText)

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
        // set cart price and etc
        setOrderParams()
        // set delivery params
        getDeliveryInfo()
        tabDelEmail = findViewById(R.id.tabDelEmail)
        tabDelCourier = findViewById(R.id.tabDelCourier)
        tabDelPoint = findViewById(R.id.tabDelPoint)
        tabDelEmail?.setOnClickListener { onClickTabDel("EMAIL") }
        tabDelCourier?.setOnClickListener { onClickTabDel("COURIER") }
        tabDelPoint?.setOnClickListener { onClickTabDel("POINT") }
        selectorButton.onSelectedListener { initToWhom(true) }
        initToWhom(true)
        // payment method
        val paymentMethod = findViewById<RadioGroup>(R.id.paymentMethod)
        paymentMethod.setOnCheckedChangeListener { group, checkedId ->
            onClickPayMethod(group)
        }
        // set up google pay button
        setupGooglePay()
        // proceed to payment
        buttonBook?.setOnClickListener { onClickBook(paymentId) }
        // back button
        val backBtn = findViewById<ImageView>(R.id.iv_nav)
        backBtn.setOnClickListener { onBackPressed() }
    }

    private fun setupGooglePay() {
        val googleParams = GooglePayParams(config.TINKOFF_TERMINAL_KEY,     // конфигурируем основные параметры
                environment = WalletConstants.ENVIRONMENT_PRODUCTION //WalletConstants.ENVIRONMENT_PRODUCTION // тестовое окружение
        )
        googlePayHelper = GooglePayHelper(googleParams) // передаем параметры в класс-помощник

        googlePayHelper!!.initGooglePay(this) { ready ->      // вызываем метод для определения доступности Google Pay на девайсе
            if (!ready) {
                val radioGooglePay = findViewById<RadioButton>(R.id.radioGooglePay)
                val googlepayLine = findViewById<View>(R.id.googlepayLine)
                radioGooglePay.visibility = View.GONE  // если Google Pay недоступен на девайсе, необходимо скрыть кнопку
                googlepayLine.visibility = View.GONE
            }
        }
    }

    /**
     * change delivery tabs
     */
    private fun onClickTabDel(type:String){
        val emailTabBlock = findViewById<LinearLayout>(R.id.delivery_email)
        val courierTabBlock = findViewById<LinearLayout>(R.id.delivery_courier)
        val pointTabBlock = findViewById<LinearLayout>(R.id.delivery_point)
        val radioCash = findViewById<RadioButton>(R.id.radioCash)
        val cashLine = findViewById<View>(R.id.cashLine)
        orderSum = settings?.getInt("orderSum", 0) ?: 0

        when(type){
            "EMAIL" -> {
                deliveryId = 3
                emailTabBlock.visibility = View.VISIBLE
                courierTabBlock.visibility = View.GONE
                pointTabBlock.visibility = View.GONE
                initToWhom(true)
                tabDelEmail!!.isChecked = true
                tabDelCourier!!.isChecked = false
                tabDelPoint!!.isChecked = false
                radioCash.visibility = View.GONE // убираем оплату наличкой
                cashLine.visibility = View.GONE
                deliveryOrderPrice = 0
            }
            "COURIER" -> {
                deliveryId = 1
                emailTabBlock.visibility = View.GONE
                courierTabBlock.visibility = View.VISIBLE
                pointTabBlock.visibility = View.GONE
                initToWhom()
                tabDelEmail!!.isChecked = false
                tabDelCourier!!.isChecked = true
                tabDelPoint!!.isChecked = false
                viewId = 0
                radioCash.visibility = View.VISIBLE
                cashLine.visibility = View.VISIBLE
                deliveryOrderPrice = if(orderSum < orderPricePay) deliveryPrice else 0
            }
            "POINT" -> {
                deliveryId = 2
                emailTabBlock.visibility = View.GONE
                courierTabBlock.visibility = View.GONE
                pointTabBlock.visibility = View.VISIBLE
                initToWhom()
                tabDelEmail!!.isChecked = false
                tabDelCourier!!.isChecked = false
                tabDelPoint!!.isChecked = true
                viewId = 0
                radioCash.visibility = View.VISIBLE
                cashLine.visibility = View.VISIBLE
                deliveryOrderPrice = if(orderSum < orderPricePickupPay) pickupPrice else 0
                setPickupPriceDelivery(findViewById(R.id.radioGroup))
            }
        }
        if(cashBackCharged){
            orderSum -= availableCashBack
        }
        val delPriceTV = findViewById<TextView>(R.id.delivery_price)
        val totalPriceTV = findViewById<TextView>(R.id.total_price)
        delPriceTV.text = "$deliveryOrderPrice ${config.RUB}"
        totalPriceTV.text = "${orderSum + deliveryOrderPrice} ${config.RUB}"
    }

    private fun initToWhom(isVisible: Boolean = false) {
        toWhomLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        etRecipientEmailText.visibility = if (selectorButton.isSecond) View.VISIBLE else View.GONE
        hintRecipientEmailText.visibility = if (selectorButton.isSecond) View.VISIBLE else View.GONE
    }

    private fun onClickPayMethod(group: RadioGroup){
        val selectedId: Int = group.checkedRadioButtonId
        val radioButton = findViewById<RadioButton>(selectedId)
        when(radioButton.text){
            "Картой online" -> {
                paymentId = REQUEST_CODE_PAYMENT
            }
            "Наличными при получении" -> {
                paymentId = CASH_CODE_PAYMENT
            }
            "Google Pay" -> {
                paymentId = GOOGLE_PAY_REQUEST_CODE
            }
        }
    }

    private fun onClickBook(type:Int) {
        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etCongratText = findViewById<EditText>(R.id.etCongratText)
        val cbPersonalAgree = findViewById<CheckBox>(R.id.personalAgree)
        clientName = etName.text.toString()
        clientPhone = etPhone.text.toString()
        clientEmail = etEmail.text.toString()
        if(!cbPersonalAgree.isChecked){
            Toast.makeText(this, resources.getString(R.string.buy_agree), Toast.LENGTH_LONG).show()
            return
        }
        if (clientName.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.enter_name), Toast.LENGTH_LONG).show()
            return
        }
        if (clientPhone.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.enter_phone), Toast.LENGTH_LONG).show()
            return
        }
        if (clientPhone.length < 7) {
            Toast.makeText(this, "Номер телефона должен содержать не менее 7 цифр", Toast.LENGTH_LONG).show()
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
        var deliveryAddress = ""
        var comment = ""
        when(deliveryId){
            1 ->{
                val delAddress = findViewById<EditText>(R.id.etAdress).text.toString()
                if (delAddress.isEmpty()) {
                    Toast.makeText(this, resources.getString(R.string.enter_delivery_address), Toast.LENGTH_LONG).show()
                    buttonBook!!.isEnabled = true
                    return
                }
                else {
                    deliveryAddress = delAddress
                    comment = findViewById<EditText>(R.id.etComment).text.toString()
                }
            }
            2->{
                val selectedId: Int = findViewById<RadioGroup>(R.id.radioGroup).checkedRadioButtonId
                val radioButton = findViewById<RadioButton>(selectedId)
                deliveryAddress = radioButton.text.toString()
            }
        }
        val usedCashback = if(cashBackCharged) availableCashBack.toString() else "0"
        mapPost["type"] = ordType.toString()
        mapPost["item_id"] = itemId.toString()
        mapPost["city_id"] = cityId.toString()
        mapPost["sum"] = orderSum.toString()
        mapPost["item_name"] = itemName
        mapPost["payment_id"] = paymentId.toString() // bank card
        mapPost["delivery_id"] = deliveryId.toString() // тип доставки
        mapPost["delivery_address"] = deliveryAddress // тип доставки
        mapPost["comment"] = comment // комментарий
        mapPost["view_id"] = viewId.toString() // шаблон электронного сертификата
        mapPost["view_text"] = etCongratText.text.toString() // текст поздравления
        mapPost["view_email"] = etRecipientEmailText.string // e-mail получателя
        mapPost["closed"] = "0" // активирован ли сертификат
        mapPost["utm"] = "android"
        mapPost["used_cashback"] = usedCashback

        val connector = ServerConnector(
            context,
            { success, output -> when (success) {
                true -> doPayment(output, type)
                false -> Toast.makeText(context, output, Toast.LENGTH_LONG).show().run {
                    buttonBook?.isEnabled = true
                }
            } },true)
        connector.execute(mapPost)
    }

    private fun doPayment(output: String, type: Int) {
        try {
            val jsonResult = JSONObject(output)
            val status = jsonResult.getBoolean("status")
            if (status) {
                ordId = jsonResult.getInt("order_id")
                // Purchase in Firebase Analytics
                var mFirebaseAnalytics: FirebaseAnalytics? = null
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
                val bundle = Bundle()
                //bundle.putString(FirebaseAnalytics.Param.VALUE, orderSum.toString())
                bundle.putDouble(FirebaseAnalytics.Param.VALUE, orderSum.toDouble());
                bundle.putString(FirebaseAnalytics.Param.CURRENCY, "RUB")
                bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, ordId.toString())
                /*val item1 = Bundle()
                item1.putString(FirebaseAnalytics.Param.ITEM_NAME, "Полет на самолете")
                item1.putString(FirebaseAnalytics.Param.ITEM_ID, "599")
                item1.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Полеты")
                bundle.putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(item1))*/
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)

                if (cashBackCharged) {
                    cashBack -= availableCashBack
                }
                if(paymentId == CASH_CODE_PAYMENT){
                    successActivity()
                }
                else {
                    when (type) {
                        REQUEST_CODE_PAYMENT -> {
                            tinkoffAcquiring.openPaymentScreen(this, createPaymentOptions(), REQUEST_CODE_PAYMENT)
                        }
                        GOOGLE_PAY_REQUEST_CODE -> {
                            googlePayHelper!!.openGooglePay(this@BuyCertActivity, Money.ofRubles(orderSum.toLong()), GOOGLE_PAY_REQUEST_CODE)
                        }
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
                        description = itemName    // описание платежа, видимое пользователю
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
                        this@BuyCertActivity,
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
        extras.putInt("paymentId", paymentId)
        extras.putInt("orderId", ordId)
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


    private fun getDeliveryInfo() {
        val mapPost: HashMap<String, String> = HashMap()
        mapPost["action"] = "delivery"
        mapPost["city_id"] = settings!!.getInt("cityId", 1).toString()
        val connector = ServerConnector(this, ServerConnector.AsyncResponse { success, output ->
            if (success) {
                try {
                    val jsonResult = JSONObject(output)
                    val status = jsonResult.getBoolean("status")
                    if (status) {
                        // ЭЛЕКТРОННЫЕ СЕРТИФИКАТЫ
                        val certificates = jsonResult.getJSONArray("certificates")
                        val certCount: Int = certificates.length()
                        val imageList = ArrayList<SlideModel>() // список изображений шаблонов
                        val viewIdList = ArrayList<Int>() // список ID шаблонов
                        if (certCount > 0) {
                            for (i in 0 until certCount){
                                val certificate: JSONObject = certificates.getJSONObject(i)
                                imageList.add(SlideModel(certificate.getString("img")))
                                viewIdList.add(certificate.getString("id").toInt())
                                if(i == 0){
                                    viewId = viewIdList[0]
                                }
                            }
                        }
                        val imageSlider = findViewById<ImageSlider>(R.id.image_slider)
                        imageSlider.setImageList(imageList, ScaleTypes.CENTER_INSIDE)
                        imageSlider.setItemChangeListener(object : ItemChangeListener {
                            override fun onItemChanged(position: Int) {
                                viewId = viewIdList[position]
                            }
                        })
                        imageSlider.setItemClickListener(object : ItemClickListener {
                            override fun onItemSelected(position: Int) {
                                startFullImage(imageList.getOrNull(position)?.imageUrl.orEmpty())
                            }

                        })
                        // ПУНКТЫ САМОВЫВОЗА
                        val pickups = jsonResult.getJSONArray("pickup")
                        val pickupCount: Int = pickups.length()
                        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
                        if (pickupCount > 0) {
                            for (i in 0 until pickupCount){
                                val pickup: JSONObject = pickups.getJSONObject(i)
                                val pickupname = pickup.getString("name")
                                val pickupid = pickup.getString("id")

                                pickupPriceDelivery.add(pickup.getString("price_delivery").toInt())

                                radioButtonView.apply {
                                    drawablesBottom(R.drawable.ic_line)
                                    compoundDrawablePadding = 16.dpToPx
                                    paddingTop(16)
                                    text = pickupname
                                    id = pickupid.toInt()
                                    isChecked = i == 0

                                    radioGroup.addView(this)
                                }
                            }
                        }
                        radioGroup.setOnCheckedChangeListener { group, _ ->
                            setPickupPriceDelivery(group)
                        }
                        // ДОСТАВКА
                        val deliveries = jsonResult.getJSONArray("delivery")
                        val deliveryCount: Int = deliveries.length()
                        if (deliveryCount > 0) {
                            for (i in 0 until deliveryCount){
                                val delivery: JSONObject = deliveries.getJSONObject(i)
                                when(delivery.getString("name")) {
                                    "Доставка курьером" -> {
                                        orderPricePay = delivery.getString("order_price_pay").toInt()
                                        deliveryPrice = delivery.getString("price").toInt()
                                    }
                                    "Самовывоз" ->{
                                        orderPricePickupPay = delivery.getString("order_price_pay").toInt()
                                        pickupPrice = delivery.getString("price").toInt()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }, false)
        connector.execute(mapPost)
    }

    private fun startFullImage(url: String) = Intent(this, FullImageActivity::class.java)
        .apply {
            putExtras(Bundle().apply { putString("imgFullUrl", url) })
            startActivity(this)
        }


    private fun getCashBack() {
        val mapPost: HashMap<String, String> = HashMap()
        mapPost["action"] = "cashback"
        mapPost["session_key"] = sessionKey!!
        val connector = ServerConnector(this, ServerConnector.AsyncResponse { success, output ->
            if (success) {
                try {
                    val jsonResult = JSONObject(output)
                    val status = jsonResult.getBoolean("status")
                    if (status) {
                        val cashBackTxt = findViewById<TextView>(R.id.total_cashback)
                        val cashBackChBox = findViewById<CheckBox>(R.id.cashbackCheck)
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
                                    + resources.getString(R.string.cashback_use2))  + " (не более " + config.MAX_CASHBACK_PERCENT * 100 +"%)";
                            cashBackChBox.text = useCashBackStr
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
        connector.execute(mapPost)
    }
//Todo
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
        val sum = "${orderSum + deliveryOrderPrice} ${config.RUB}"
        val totalPriceTxt = findViewById<TextView>(R.id.total_price)
        val usedCashBackTxt = findViewById<TextView>(R.id.used_cashback)
        val earnCashBackTxt = findViewById<TextView>(R.id.earnCashBack)
        val earnCashBack = (orderSum * config.CASHBACK).roundToInt()
        val earnCashBackStr = "<font color='" + resources.getColor(R.color.colorAccent) + "'>+" + earnCashBack + " " + config.RUB + "</font> " + resources.getString(R.string.get_bonus2)
        totalPriceTxt.text = sum
        usedCashBackTxt.text = usedCashBack
        earnCashBackTxt.text = Html.fromHtml(earnCashBackStr)
    }

    private fun setOrderParams() {
        ordType = settings?.getString("ordType", "impression")
        val cityDeliveryTxt: TextView = findViewById(R.id.etCity)
        cityDeliveryTxt.text = settings?.getString("cityName", "Москва")
        cityId = settings!!.getInt("cityId", 1)
        itemId = settings!!.getString("itemId", "")
        orderSum = settings!!.getInt("orderSum", 0)
        orderOldSum = settings!!.getInt("price_old", 0)
        itemName = settings!!.getString("itemName", "")!!
        clientPhone = settings!!.getString("clientPhone", "")!!
        clientEmail = settings!!.getString("clientEmail", "")!!
        clientName = settings!!.getString("clientName", "")!!
        val impOfferNameTxt = findViewById<TextView>(R.id.itemName)
        val durationNameTxt = findViewById<TextView>(R.id.duration_name)
        val humanNameTxt = findViewById<TextView>(R.id.human_name)
        val impSumTxt = findViewById<TextView>(R.id.impSum)
        val impcountBlock = findViewById<RelativeLayout>(R.id.impcountBlock)
        val humanNameBlock = findViewById<RelativeLayout>(R.id.humanNameBlock)
        val durationBlock = findViewById<RelativeLayout>(R.id.durationBlock)

        impOfferNameTxt.text = itemName
        if(ordType!! == "set"){
            val impCount = findViewById<TextView>(R.id.impCount)
            val impInSet = settings!!.getString("impressionCount", "")!!
            impCount.text = "В набор входит $impInSet впечатление на выбор"
            val setSum = findViewById<TextView>(R.id.setSum)
            setSum.text = orderSum.toString() + " " + config.RUB

            impcountBlock.visibility = View.VISIBLE
            humanNameBlock.visibility = View.GONE
            durationBlock.visibility = View.GONE
        }
        else{
            human_name = settings!!.getString("human_name", "")!!
            duration_name = settings!!.getString("duration_name", "")!!
            durationNameTxt.text = duration_name
            humanNameTxt.text = human_name

            impcountBlock.visibility = View.GONE
            humanNameBlock.visibility = View.VISIBLE
            durationBlock.visibility = View.VISIBLE
        }

        val totalPriceTxt = findViewById<TextView>(R.id.total_price)
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
        impSumTxt.text = sum

        val earnCashBack = (orderSum * config.CASHBACK).roundToInt()
        val earnCashBackStr = "<font color='" + resources.getColor(R.color.colorAccent) + "'>+" + earnCashBack + " " + config.RUB + "</font> " + resources.getString(R.string.get_bonus2)

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

    private val radioButtonView get() = layoutInflater
        .inflate(R.layout.radio_button, null, false) as RadioButton

    @SuppressLint("SetTextI18n")
    private fun setPickupPriceDelivery(group: RadioGroup){
        val selectedId: Int = group.checkedRadioButtonId
        val radioButton = findViewById<RadioButton>(selectedId)
        val index = group.indexOfChild(radioButton)
        deliveryOrderPrice = if(orderSum < orderPricePickupPay) pickupPriceDelivery[index] else 0
        val delPriceTV = findViewById<TextView>(R.id.delivery_price)
        val totalPriceTV = findViewById<TextView>(R.id.total_price)
        delPriceTV.text = "$deliveryOrderPrice ${config.RUB}"
        totalPriceTV.text = "${orderSum + deliveryOrderPrice} ${config.RUB}"
    }
}