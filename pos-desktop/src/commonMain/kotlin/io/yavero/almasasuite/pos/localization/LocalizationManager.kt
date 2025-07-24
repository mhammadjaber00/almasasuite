package io.yavero.almasasuite.pos.localization

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class LocalizationManager {
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val englishStrings = mapOf(

        StringResources.OK to "OK",
        StringResources.CANCEL to "Cancel",
        StringResources.ERROR to "Error",
        StringResources.SAVE to "Save",
        StringResources.DELETE to "Delete",
        StringResources.EDIT to "Edit",
        StringResources.ADD to "Add",
        StringResources.SEARCH to "Search",
        StringResources.LOADING to "Loading",
        StringResources.YES to "Yes",
        StringResources.NO to "No",


        StringResources.LOGIN to "Login",
        StringResources.LOGOUT to "Logout",
        StringResources.ENTER_PIN to "Enter PIN",
        StringResources.ENTER_MANAGER_PIN to "Enter Manager PIN",
        StringResources.PIN to "PIN",
        StringResources.CONFIRM_LOGOUT to "Confirm Logout",
        StringResources.CONFIRM_LOGOUT_MESSAGE to "Are you sure you want to logout?",


        StringResources.SALES to "Sales",
        StringResources.INVENTORY to "Inventory",
        StringResources.PURCHASES to "Purchases",
        StringResources.REPORTS to "Reports",
        StringResources.USERS to "Users",
        StringResources.DAILY_SALES to "Daily Sales",
        StringResources.POS_SYSTEM to "POS System",


        StringResources.ADD_PRODUCT to "Add Product",
        StringResources.ADD_JEWELRY_PRODUCT to "Add Jewelry Product",
        StringResources.EDIT_JEWELRY_PRODUCT to "Edit Jewelry Product",
        StringResources.ADD_YOUR_FIRST_PRODUCT to "Add Your First Product",
        StringResources.SEARCH_PRODUCTS to "Search products...",
        StringResources.SEARCH_BY_SKU to "Search by SKU...",
        StringResources.ALL_TYPES to "All Types",
        StringResources.ALL to "All",
        StringResources.LOW_STOCK to "Low Stock",
        StringResources.EXPORT_CSV to "Export CSV",
        StringResources.SKU to "SKU",
        StringResources.SKU_OPTIONAL to "SKU (optional)",
        StringResources.IMAGE_URL_OPTIONAL to "Image URL (optional)",
        StringResources.JEWELRY_TYPE to "Jewelry Type",
        StringResources.KARAT to "Karat",
        StringResources.WEIGHT_GRAMS to "Weight (grams)",
        StringResources.DESIGN_FEE to "Design Fee",
        StringResources.PURCHASE_PRICE to "Purchase Price",
        StringResources.TOTAL_PRICE to "Total Price",
        StringResources.QUANTITY_IN_STOCK to "Quantity in Stock",
        StringResources.NAME to "Name",
        StringResources.STONE to "Stone",
        StringResources.CARAT to "Carat",
        StringResources.WEIGHT to "Weight",


        StringResources.ADJUST_STOCK to "Adjust Stock",
        StringResources.ADJUST_JEWELRY_STOCK to "Adjust Jewelry Stock",
        StringResources.CURRENT_STOCK to "Current Stock",
        StringResources.ADJUSTMENT to "Adjustment",
        StringResources.ADJUSTMENT_PLACEHOLDER to "e.g., +5 or -2",
        StringResources.REASON to "Reason",


        StringResources.PRICE to "Price",
        StringResources.PRICE_PLACEHOLDER to "0.00",
        StringResources.QUANTITY to "Quantity",
        StringResources.QTY to "Qty",
        StringResources.SALE_PRICE to "Sale Price *",
        StringResources.SUBTOTAL to "Subtotal",
        StringResources.TOTAL to "Total",
        StringResources.CHECKOUT to "Checkout",


        StringResources.ADD_TO_SALES_LOG to "Add to Sales Log",
        StringResources.SUBMIT_DAYS_SALES to "Submit Day's Sales",
        StringResources.SUBMIT_DAYS_SALES_CONFIRM to "Submit Day's Sales",
        StringResources.SUBMIT_DAYS_SALES_MESSAGE to "Are you sure you want to submit today's sales?",
        StringResources.SUBMIT_SALES to "Submit Sales",
        StringResources.DAILY_SALES_LOGGING to "Daily Sales Logging",
        StringResources.ITEMS_COUNT to "items",
        StringResources.TOTAL_VALUE to "Total value",
        StringResources.SCAN_OR_ENTER_SKU to "Scan or Enter SKU",
        StringResources.STOP_SCANNING to "Stop Scanning",
        StringResources.SCAN_BARCODE to "Scan Barcode",
        StringResources.PLEASE_ENTER_SKU to "Please enter or scan a SKU",
        StringResources.PLEASE_ENTER_VALID_PRICE to "Please enter a valid sale price",
        StringResources.PRODUCT_FOR to "Product for",


        StringResources.TODAY to "Today",
        StringResources.LAST_7_DAYS to "Last 7 Days",
        StringResources.TO to "to",


        StringResources.RECORD_GOLD_INTAKE to "Record Gold Intake",
        StringResources.SELLER to "Seller",
        StringResources.CUSTOMER to "Customer",
        StringResources.ENTER_NAME to "Enter name",
        StringResources.GRAMS to "Grams",
        StringResources.DESIGN_FEE_PER_GRAM to "Design Fee per Gram",
        StringResources.METAL_VALUE_PER_GRAM to "Metal Value per Gram (Owed)",
        StringResources.NOTES_OPTIONAL to "Notes (Optional)",
        StringResources.ADDITIONAL_INFORMATION to "Additional information...",
        StringResources.RECORD_INTAKE to "Record Intake",
        StringResources.RECORD_VENDOR_PAYMENT to "Record Vendor Payment",
        StringResources.VENDOR to "Vendor",
        StringResources.PAYMENT_AMOUNT to "Payment Amount",
        StringResources.REFERENCE_OPTIONAL to "Reference (Optional)",
        StringResources.CHECK_NUMBER_TRANSFER_ID to "Check number, transfer ID, etc.",
        StringResources.RECORD_PAYMENT to "Record Payment",
        StringResources.PAY_VENDOR to "Pay Vendor",


        StringResources.CURRENT_STOCK_UNITS to "Current stock: {} units",
        StringResources.ADJUSTMENT_PLUS_MINUS to "Adjustment (+/-)",
        StringResources.ADJUST_STOCK_TITLE to "Adjust Stock - {}",
        StringResources.PRODUCT_INFO to "Product: {} {}K {}g",
        StringResources.ADJUST to "Adjust",


        StringResources.COPY to "Copy",
        StringResources.PRINT to "Print",
        StringResources.PRODUCT_BARCODE to "Product Barcode",
        StringResources.CLOSE to "Close",
        StringResources.SCAN to "Scan",
        StringResources.SYNC_NOW to "Sync Now",


        StringResources.PRINT_REPORT to "Print Report",
        StringResources.JEWELRY_MODE to "Jewelry Mode",


        StringResources.ADD_JEWELRY_PRODUCT_BUTTON to "Add Jewelry Product",
        StringResources.REGULAR_INVENTORY_PANEL to "Regular Inventory Panel",
        StringResources.REGULAR_PRODUCT_LIST to "Regular Product List",


        StringResources.ADD_NEW_USER to "Add New User",
        StringResources.USER_MANAGEMENT_DESCRIPTION to "Create, edit, and manage user accounts",


        StringResources.APP_TITLE to "ALMASA POS",
        StringResources.ALMASA_POS to "ALMASA POS",
        StringResources.ALMASA_JEWELRY_POS to "ALMASA JEWELRY POS",


        StringResources.LANGUAGE to "Language",
        StringResources.ENGLISH to "English",
        StringResources.ARABIC to "Arabic",
        StringResources.SWITCH_LANGUAGE to "Switch Language",


        StringResources.DECREASE_QUANTITY to "Decrease Quantity",
        StringResources.INCREASE_QUANTITY to "Increase Quantity",
        StringResources.REMOVE_ITEM to "Remove Item",


        StringResources.PAYMENT_METHOD to "Payment Method",
        StringResources.CASH to "Cash",
        StringResources.CARD to "Card",
        StringResources.PROCESSING to "Processing..."
    )

    private val arabicStrings = mapOf(

        StringResources.OK to "موافق",
        StringResources.CANCEL to "إلغاء",
        StringResources.ERROR to "خطأ",
        StringResources.SAVE to "حفظ",
        StringResources.DELETE to "حذف",
        StringResources.EDIT to "تعديل",
        StringResources.ADD to "إضافة",
        StringResources.SEARCH to "بحث",
        StringResources.LOADING to "جاري التحميل",
        StringResources.YES to "نعم",
        StringResources.NO to "لا",


        StringResources.LOGIN to "تسجيل الدخول",
        StringResources.LOGOUT to "تسجيل الخروج",
        StringResources.ENTER_PIN to "أدخل الرقم السري",
        StringResources.ENTER_MANAGER_PIN to "أدخل رقم المدير السري",
        StringResources.PIN to "الرقم السري",
        StringResources.CONFIRM_LOGOUT to "تأكيد تسجيل الخروج",
        StringResources.CONFIRM_LOGOUT_MESSAGE to "هل أنت متأكد من تسجيل الخروج؟",


        StringResources.SALES to "المبيعات",
        StringResources.INVENTORY to "المخزون",
        StringResources.PURCHASES to "المشتريات",
        StringResources.REPORTS to "التقارير",
        StringResources.USERS to "المستخدمون",
        StringResources.DAILY_SALES to "المبيعات اليومية",
        StringResources.POS_SYSTEM to "نظام نقاط البيع",


        StringResources.ADD_PRODUCT to "إضافة منتج",
        StringResources.ADD_JEWELRY_PRODUCT to "إضافة منتج مجوهرات",
        StringResources.EDIT_JEWELRY_PRODUCT to "تعديل منتج مجوهرات",
        StringResources.ADD_YOUR_FIRST_PRODUCT to "أضف منتجك الأول",
        StringResources.SEARCH_PRODUCTS to "البحث عن المنتجات...",
        StringResources.SEARCH_BY_SKU to "البحث بالرمز...",
        StringResources.ALL_TYPES to "جميع الأنواع",
        StringResources.ALL to "الكل",
        StringResources.LOW_STOCK to "مخزون منخفض",
        StringResources.EXPORT_CSV to "تصدير CSV",
        StringResources.SKU to "الرمز",
        StringResources.SKU_OPTIONAL to "الرمز (اختياري)",
        StringResources.IMAGE_URL_OPTIONAL to "رابط الصورة (اختياري)",
        StringResources.JEWELRY_TYPE to "نوع المجوهرات",
        StringResources.KARAT to "العيار",
        StringResources.WEIGHT_GRAMS to "الوزن (جرام)",
        StringResources.DESIGN_FEE to "رسوم التصميم",
        StringResources.PURCHASE_PRICE to "سعر الشراء",
        StringResources.TOTAL_PRICE to "السعر الإجمالي",
        StringResources.QUANTITY_IN_STOCK to "الكمية في المخزون",
        StringResources.NAME to "الاسم",
        StringResources.STONE to "الحجر",
        StringResources.CARAT to "القيراط",
        StringResources.WEIGHT to "الوزن",


        StringResources.ADJUST_STOCK to "تعديل المخزون",
        StringResources.ADJUST_JEWELRY_STOCK to "تعديل مخزون المجوهرات",
        StringResources.CURRENT_STOCK to "المخزون الحالي",
        StringResources.ADJUSTMENT to "التعديل",
        StringResources.ADJUSTMENT_PLACEHOLDER to "مثال: +5 أو -2",
        StringResources.REASON to "السبب",


        StringResources.PRICE to "السعر",
        StringResources.PRICE_PLACEHOLDER to "0.00",
        StringResources.QUANTITY to "الكمية",
        StringResources.QTY to "الكمية",
        StringResources.SALE_PRICE to "سعر البيع *",
        StringResources.SUBTOTAL to "المجموع الفرعي",
        StringResources.TOTAL to "الإجمالي",
        StringResources.CHECKOUT to "الدفع",


        StringResources.ADD_TO_SALES_LOG to "إضافة إلى سجل المبيعات",
        StringResources.SUBMIT_DAYS_SALES to "إرسال مبيعات اليوم",
        StringResources.SUBMIT_DAYS_SALES_CONFIRM to "إرسال مبيعات اليوم",
        StringResources.SUBMIT_DAYS_SALES_MESSAGE to "هل أنت متأكد من إرسال مبيعات اليوم؟",
        StringResources.SUBMIT_SALES to "إرسال المبيعات",
        StringResources.DAILY_SALES_LOGGING to "تسجيل المبيعات اليومية",
        StringResources.ITEMS_COUNT to "عناصر",
        StringResources.TOTAL_VALUE to "القيمة الإجمالية",
        StringResources.SCAN_OR_ENTER_SKU to "امسح أو أدخل الرمز",
        StringResources.STOP_SCANNING to "إيقاف المسح",
        StringResources.SCAN_BARCODE to "مسح الباركود",
        StringResources.PLEASE_ENTER_SKU to "يرجى إدخال أو مسح الرمز",
        StringResources.PLEASE_ENTER_VALID_PRICE to "يرجى إدخال سعر بيع صحيح",
        StringResources.PRODUCT_FOR to "منتج لـ",


        StringResources.TODAY to "اليوم",
        StringResources.LAST_7_DAYS to "آخر 7 أيام",
        StringResources.TO to "إلى",


        StringResources.RECORD_GOLD_INTAKE to "تسجيل استلام الذهب",
        StringResources.SELLER to "البائع",
        StringResources.CUSTOMER to "العميل",
        StringResources.ENTER_NAME to "أدخل الاسم",
        StringResources.GRAMS to "جرام",
        StringResources.DESIGN_FEE_PER_GRAM to "رسوم التصميم لكل جرام",
        StringResources.METAL_VALUE_PER_GRAM to "قيمة المعدن لكل جرام (مستحقة)",
        StringResources.NOTES_OPTIONAL to "ملاحظات (اختيارية)",
        StringResources.ADDITIONAL_INFORMATION to "معلومات إضافية...",
        StringResources.RECORD_INTAKE to "تسجيل الاستلام",
        StringResources.RECORD_VENDOR_PAYMENT to "تسجيل دفعة المورد",
        StringResources.VENDOR to "المورد",
        StringResources.PAYMENT_AMOUNT to "مبلغ الدفع",
        StringResources.REFERENCE_OPTIONAL to "المرجع (اختياري)",
        StringResources.CHECK_NUMBER_TRANSFER_ID to "رقم الشيك، معرف التحويل، إلخ.",
        StringResources.RECORD_PAYMENT to "تسجيل الدفع",
        StringResources.PAY_VENDOR to "دفع للمورد",


        StringResources.CURRENT_STOCK_UNITS to "المخزون الحالي: {} وحدة",
        StringResources.ADJUSTMENT_PLUS_MINUS to "التعديل (+/-)",
        StringResources.ADJUST_STOCK_TITLE to "تعديل المخزون - {}",
        StringResources.PRODUCT_INFO to "المنتج: {} {}ك {}ج",
        StringResources.ADJUST to "تعديل",


        StringResources.COPY to "نسخ",
        StringResources.PRINT to "طباعة",
        StringResources.PRODUCT_BARCODE to "باركود المنتج",
        StringResources.CLOSE to "إغلاق",
        StringResources.SCAN to "مسح",
        StringResources.SYNC_NOW to "مزامنة الآن",


        StringResources.PRINT_REPORT to "طباعة التقرير",
        StringResources.JEWELRY_MODE to "وضع المجوهرات",


        StringResources.ADD_JEWELRY_PRODUCT_BUTTON to "إضافة منتج مجوهرات",
        StringResources.REGULAR_INVENTORY_PANEL to "لوحة المخزون العادية",
        StringResources.REGULAR_PRODUCT_LIST to "قائمة المنتجات العادية",


        StringResources.ADD_NEW_USER to "إضافة مستخدم جديد",
        StringResources.USER_MANAGEMENT_DESCRIPTION to "إنشاء وتعديل وإدارة حسابات المستخدمين",


        StringResources.APP_TITLE to "نقاط بيع الماسة",
        StringResources.ALMASA_POS to "نقاط بيع الماسة",
        StringResources.ALMASA_JEWELRY_POS to "نقاط بيع مجوهرات الماسة",


        StringResources.LANGUAGE to "اللغة",
        StringResources.ENGLISH to "الإنجليزية",
        StringResources.ARABIC to "العربية",
        StringResources.SWITCH_LANGUAGE to "تغيير اللغة",


        StringResources.DECREASE_QUANTITY to "تقليل الكمية",
        StringResources.INCREASE_QUANTITY to "زيادة الكمية",
        StringResources.REMOVE_ITEM to "إزالة العنصر",


        StringResources.PAYMENT_METHOD to "طريقة الدفع",
        StringResources.CASH to "نقداً",
        StringResources.CARD to "بطاقة",
        StringResources.PROCESSING to "جاري المعالجة..."
    )

    fun getString(key: String): String {
        val strings = when (_currentLanguage.value) {
            Language.ENGLISH -> englishStrings
            Language.ARABIC -> arabicStrings
        }
        return strings[key] ?: key
    }

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
    }

    fun toggleLanguage() {
        _currentLanguage.value = when (_currentLanguage.value) {
            Language.ENGLISH -> Language.ARABIC
            Language.ARABIC -> Language.ENGLISH
        }
    }
}

val LocalLocalizationManager = compositionLocalOf<LocalizationManager> {
    error("LocalizationManager not provided")
}

@Composable
fun rememberLocalizationManager(): LocalizationManager {
    return LocalLocalizationManager.current
}

@Composable
fun getString(key: String): String {
    val localizationManager = rememberLocalizationManager()
    return localizationManager.getString(key)
}