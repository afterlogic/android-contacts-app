package com.afterlogic.auroracontacts.data.api.p7.model

import com.google.gson.annotations.SerializedName

/**
 * Created by sashka on 18.03.16.
 * mail: sunnyday.development@gmail.com
 */
class SystemAppDataP7 {

    /**
     * Auth : true
     * UserP8 : {"IdUser":22,"MailsPerPage":20,"ContactsPerPage":20,"AutoCheckMailInterval":1,"DefaultEditor":1,"Layout":0,"LoginsCount":697,"CanLoginWithPassword":true,"DefaultTheme":"Default","DefaultLanguage":"English","DefaultLanguageShort":"en","DefaultDateFormat":"MM/DD/YYYY","DefaultTimeFormat":0,"DefaultTimeZone":"Europe/Moscow","AllowCompose":true,"AllowReply":true,"AllowForward":true,"AllowFetcher":true,"SaveMail":0,"ThreadsEnabled":true,"UseThreads":true,"SaveRepliedMessagesToCurrentFolder":true,"DesktopNotifications":false,"AllowChangeInputDirection":false,"EnableOpenPgp":false,"AllowAutosaveInDrafts":true,"AutosignOutgoingEmails":false,"EmailNotification":"test@afterlogic.com","OutlookSyncEnable":true,"MobileSyncEnable":true,"ShowPersonalContacts":true,"ShowGlobalContacts":true,"IsCollaborationSupported":true,"AllowFilesSharing":false,"IsFilesSupported":true,"IsHelpdeskSupported":true,"IsHelpdeskAgent":true,"AllowHelpdeskNotifications":false,"HelpdeskSignature":"","HelpdeskSignatureEnable":false,"LastLoggedHost":0,"AllowVoice":true,"VoiceProvider":"twilio","SipRealm":"","SipWebsocketProxyUrl":"","SipOutboundProxyUrl":"","SipCallerID":"","TwilioNumber":"","TwilioEnable":true,"SipEnable":true,"SipImpi":"","SipPassword":"","FilesEnable":true,"AllowCalendar":true,"Calendar":{"ShowWeekEnds":false,"ShowWorkDay":true,"WorkDayStarts":9,"WorkDayEnds":18,"WeekStartsOn":1,"DefaultTab":3,"SyncLogin":"test@afterlogic.com","DavServerUrl":"https://p7-dav.afterlogic.com","DavPrincipalUrl":"https://p7-dav.afterlogic.com/principals/test@afterlogic.com","AllowReminders":true},"CalendarSharing":true,"CalendarAppointments":true,"IsDemo":false}
     * TenantHash :
     * IsMobile : -1
     * AllowMobile : true
     * IsMailsuite : false
     * HelpdeskSiteName :
     * HelpdeskIframeUrl :
     * HelpdeskRedirect : false
     * HelpdeskThreadId : 0
     * HelpdeskActivatedEmail :
     * HelpdeskForgotHash :
     * ClientDebug : false
     * MailExpandFolders : false
     * HtmlEditorDefaultFontName :
     * HtmlEditorDefaultFontSize :
     * AllowSaveAsPdf : true
     * LastErrorCode : 0
     * Token : 3e55fabeda74c940e37333c9eea06a40
     * ZipAttachments : true
     * AllowIdentities : true
     * SocialEmail :
     * SocialIsLoggedIn : false
     * Links : {"ImportingContacts":"http://www.afterlogic.com/wiki/Importing_contacts_(Aurora)","OutlookSyncPlugin32":"http://www.afterlogic.com/download/OutlookSyncAddIn.msi","OutlookSyncPlugin64":"http://www.afterlogic.com/download/OutlookSyncAddIn64.msi","OutlookSyncPluginReadMore":"http://www.afterlogic.com/wiki/Outlook_sync_(Aurora)"}
     * SocialGoogle : true
     * SocialGoogleId : 631514845250-lsi1lf1j7vqsb5rq18lh4t499glp7f8b.apps.googleusercontent.com
     * SocialGoogleScopes : auth filestorage
     * SocialDropbox : true
     * SocialDropboxId : fl0eoztbakx121p
     * SocialDropboxScopes : auth filestorage
     * SocialFacebook : true
     * SocialFacebookId : 1582615142003625
     * SocialFacebookScopes : auth
     * SocialTwitter : true
     * SocialTwitterId : 5dK6c7peT49vQXRi5JBR3HcRg
     * SocialTwitterScopes : auth
     * Plugins : {"ExternalServices":{"Connectors":[{"@Object":"Object/CTenantSocials","Id":"631514845250-lsi1lf1j7vqsb5rq18lh4t499glp7f8b.apps.googleusercontent.com","Name":"Google","LowerName":"google","Allow":true,"Scopes":["auth","filestorage"]},{"@Object":"Object/CTenantSocials","Id":"fl0eoztbakx121p","Name":"Dropbox","LowerName":"dropbox","Allow":true,"Scopes":["auth","filestorage"]},{"@Object":"Object/CTenantSocials","Id":"1582615142003625","Name":"Facebook","LowerName":"facebook","Allow":true,"Scopes":["auth"]},{"@Object":"Object/CTenantSocials","Id":"5dK6c7peT49vQXRi5JBR3HcRg","Name":"Twitter","LowerName":"twitter","Allow":true,"Scopes":["auth"]}]}}
     * AllowChangePassword : true
     * LoginStyleImage :
     * AppStyleImage : https://static.afterlogic.com/img/afterlogic-logo-color-simple.png
     * HelpdeskStyleImage :
     * HelpdeskThreadAction :
     * Default : 49
     * Accounts : [{"AccountID":49,"Email":"test@afterlogic.com","FriendlyName":"Alex Orlov","Signature":{"Signature":"<div data-crea=\"font-wrapper\" style=\"font-family: Tahoma; font-size: 16px; direction: ltr\></div>">/ Alex<br></br><\/div>","Type":1,"Options":1},"IsPasswordSpecified":true,"AllowMail":true}]
     * App : {"AllowUsersChangeInterfaceSettings":true,"AllowUsersChangeEmailSettings":true,"AllowUsersAddNewAccounts":true,"AllowOpenPGP":true,"AllowWebMail":true,"DefaultTab":"mailbox","AllowIosProfile":true,"PasswordMinLength":0,"PasswordMustBeComplex":false,"AllowRegistration":false,"AllowPasswordReset":false,"RegistrationDomains":[],"RegistrationQuestions":[],"SiteName":"AfterLogic WebMail Pro","DefaultLanguage":"English","DefaultLanguageShort":"en","DefaultTheme":"Default","Languages":[{"name":"English","value":"English"},{"name":"فارسی","value":"Persian"},{"name":"Română","value":"Romanian"},{"name":"Português Brasileiro","value":"Portuguese-Brazil"},{"name":"Українська","value":"Ukrainian"},{"name":"eesti","value":"Estonian"},{"name":"Русский","value":"Russian"},{"name":"Lietuvių","value":"Lithuanian"},{"name":"tiếng Việt","value":"Vietnamese"},{"name":"Italiano","value":"Italian"},{"name":"Français","value":"French"},{"name":"العربية","value":"Arabic"},{"name":"Norsk","value":"Norwegian"},{"name":"中文(香港)","value":"Chinese-Traditional"},{"name":"Suomi","value":"Finnish"},{"name":"Nederlands","value":"Dutch"},{"name":"עברית","value":"Hebrew"},{"name":"ภาษาไทย","value":"Thai"},{"name":"Svenska","value":"Swedish"},{"name":"Slovenščina","value":"Slovenian"},{"name":"日本語","value":"Japanese"},{"name":"Magyar","value":"Hungarian"},{"name":"Español","value":"Spanish"},{"name":"Ελληνικά","value":"Greek"},{"name":"Български","value":"Bulgarian"},{"name":"Türkçe","value":"Turkish"},{"name":"Polski","value":"Polish"},{"name":"中文(简体)","value":"Chinese-Simplified"},{"name":"Deutsch","value":"German"},{"name":"Čeština","value":"Czech"},{"name":"Latviešu","value":"Latvian"},{"name":"Português","value":"Portuguese-Portuguese"},{"name":"한국어","value":"Korean"},{"name":"Srpski","value":"Serbian"},{"name":"Dansk","value":"Danish"}],"Themes":["Default","White","Blue","DeepForest","OpenWater","Autumn","BlueJeans","Ecloud","Funny"],"DateFormats":["MM/DD/YYYY","DD/MM/YYYY","DD Month YYYY"],"AttachmentSizeLimit":0,"ImageUploadSizeLimit":0,"FileSizeLimit":0,"AutoSave":true,"IdleSessionTimeout":0,"AllowInsertImage":true,"AllowBodySize":false,"MaxBodySize":600,"MaxSubjectSize":255,"JoinReplyPrefixes":true,"AllowAppRegisterMailto":true,"AllowPrefetch":true,"AllowLanguageOnLogin":true,"FlagsLangSelect":true,"LoginFormType":0,"LoginSignMeType":0,"LoginAtDomainValue":"","DemoWebMail":false,"DemoWebMailLogin":"","DemoWebMailPassword":"","GoogleAnalyticsAccount":"","CustomLoginUrl":"","CustomLogoutUrl":"","ShowQuotaBar":true,"ServerUseUrlRewrite":true,"ServerUrlRewriteBase":"https://p7.afterlogic.com/","IosDetectOnLogin":true,"AllowContactsSharing":true,"LoginDescription":""}
     */

    @SerializedName("Auth")
    var isAuthorized: Boolean = false
        private set
    /**
     * IdUser : 22
     * MailsPerPage : 20
     * ContactsPerPage : 20
     * AutoCheckMailInterval : 1
     * DefaultEditor : 1
     * Layout : 0
     * LoginsCount : 697
     * CanLoginWithPassword : true
     * DefaultTheme : Default
     * DefaultLanguage : English
     * DefaultLanguageShort : en
     * DefaultDateFormat : MM/DD/YYYY
     * DefaultTimeFormat : 0
     * DefaultTimeZone : Europe/Moscow
     * AllowCompose : true
     * AllowReply : true
     * AllowForward : true
     * AllowFetcher : true
     * SaveMail : 0
     * ThreadsEnabled : true
     * UseThreads : true
     * SaveRepliedMessagesToCurrentFolder : true
     * DesktopNotifications : false
     * AllowChangeInputDirection : false
     * EnableOpenPgp : false
     * AllowAutosaveInDrafts : true
     * AutosignOutgoingEmails : false
     * EmailNotification : test@afterlogic.com
     * OutlookSyncEnable : true
     * MobileSyncEnable : true
     * ShowPersonalContacts : true
     * ShowGlobalContacts : true
     * IsCollaborationSupported : true
     * AllowFilesSharing : false
     * IsFilesSupported : true
     * IsHelpdeskSupported : true
     * IsHelpdeskAgent : true
     * AllowHelpdeskNotifications : false
     * HelpdeskSignature :
     * HelpdeskSignatureEnable : false
     * LastLoggedHost : 0
     * AllowVoice : true
     * VoiceProvider : twilio
     * SipRealm :
     * SipWebsocketProxyUrl :
     * SipOutboundProxyUrl :
     * SipCallerID :
     * TwilioNumber :
     * TwilioEnable : true
     * SipEnable : true
     * SipImpi :
     * SipPassword :
     * FilesEnable : true
     * AllowCalendar : true
     * Calendar : {"ShowWeekEnds":false,"ShowWorkDay":true,"WorkDayStarts":9,"WorkDayEnds":18,"WeekStartsOn":1,"DefaultTab":3,"SyncLogin":"test@afterlogic.com","DavServerUrl":"https://p7-dav.afterlogic.com","DavPrincipalUrl":"https://p7-dav.afterlogic.com/principals/test@afterlogic.com","AllowReminders":true}
     * CalendarSharing : true
     * CalendarAppointments : true
     * IsDemo : false
     */

    @SerializedName("User")
    var user: User? = null
    @SerializedName("TenantHash")
    var tenantHash: String? = null
    @SerializedName("IsMobile")
    var isMobile: Int = 0
    @SerializedName("AllowMobile")
    var isAllowMobile: Boolean = false
    @SerializedName("IsMailsuite")
    var isIsMailsuite: Boolean = false
    @SerializedName("HelpdeskSiteName")
    var helpdeskSiteName: String? = null
    @SerializedName("HelpdeskIframeUrl")
    var helpdeskIframeUrl: String? = null
    @SerializedName("HelpdeskRedirect")
    var isHelpdeskRedirect: Boolean = false
    @SerializedName("HelpdeskThreadId")
    var helpdeskThreadId: Int = 0
    @SerializedName("HelpdeskActivatedEmail")
    var helpdeskActivatedEmail: String? = null
    @SerializedName("HelpdeskForgotHash")
    var helpdeskForgotHash: String? = null
    @SerializedName("ClientDebug")
    var isClientDebug: Boolean = false
    @SerializedName("MailExpandFolders")
    var isMailExpandFolders: Boolean = false
    @SerializedName("HtmlEditorDefaultFontName")
    var htmlEditorDefaultFontName: String? = null
    @SerializedName("HtmlEditorDefaultFontSize")
    var htmlEditorDefaultFontSize: String? = null
    @SerializedName("AllowSaveAsPdf")
    var isAllowSaveAsPdf: Boolean = false
    @SerializedName("LastErrorCode")
    var lastErrorCode: Int = 0
    @SerializedName("Token")
    var token: String? = null
    @SerializedName("ZipAttachments")
    var isZipAttachments: Boolean = false
    @SerializedName("AllowIdentities")
    var isAllowIdentities: Boolean = false
    @SerializedName("SocialEmail")
    var socialEmail: String? = null
    @SerializedName("SocialIsLoggedIn")
    var isSocialIsLoggedIn: Boolean = false
    /**
     * ImportingContacts : http://www.afterlogic.com/wiki/Importing_contacts_(Aurora)
     * OutlookSyncPlugin32 : http://www.afterlogic.com/download/OutlookSyncAddIn.msi
     * OutlookSyncPlugin64 : http://www.afterlogic.com/download/OutlookSyncAddIn64.msi
     * OutlookSyncPluginReadMore : http://www.afterlogic.com/wiki/Outlook_sync_(Aurora)
     */

    @SerializedName("Links")
    var links: Links? = null
    @SerializedName("SocialGoogle")
    var isSocialGoogle: Boolean = false
    @SerializedName("SocialGoogleId")
    var socialGoogleId: String? = null
    @SerializedName("SocialGoogleScopes")
    var socialGoogleScopes: String? = null
    @SerializedName("SocialDropbox")
    var isSocialDropbox: Boolean = false
    @SerializedName("SocialDropboxId")
    var socialDropboxId: String? = null
    @SerializedName("SocialDropboxScopes")
    var socialDropboxScopes: String? = null
    @SerializedName("SocialFacebook")
    var isSocialFacebook: Boolean = false
    @SerializedName("SocialFacebookId")
    var socialFacebookId: String? = null
    @SerializedName("SocialFacebookScopes")
    var socialFacebookScopes: String? = null
    @SerializedName("SocialTwitter")
    var isSocialTwitter: Boolean = false
    @SerializedName("SocialTwitterId")
    var socialTwitterId: String? = null
    @SerializedName("SocialTwitterScopes")
    var socialTwitterScopes: String? = null
    /**
     * ExternalServices : {"Connectors":[{"@Object":"Object/CTenantSocials","Id":"631514845250-lsi1lf1j7vqsb5rq18lh4t499glp7f8b.apps.googleusercontent.com","Name":"Google","LowerName":"google","Allow":true,"Scopes":["auth","filestorage"]},{"@Object":"Object/CTenantSocials","Id":"fl0eoztbakx121p","Name":"Dropbox","LowerName":"dropbox","Allow":true,"Scopes":["auth","filestorage"]},{"@Object":"Object/CTenantSocials","Id":"1582615142003625","Name":"Facebook","LowerName":"facebook","Allow":true,"Scopes":["auth"]},{"@Object":"Object/CTenantSocials","Id":"5dK6c7peT49vQXRi5JBR3HcRg","Name":"Twitter","LowerName":"twitter","Allow":true,"Scopes":["auth"]}]}
     */

    // FIXME: Empty array ([] not object or null) received when its empty
    @SerializedName("Plugins")
    var plugins: Plugins? = null
    @SerializedName("AllowChangePassword")
    var isAllowChangePassword: Boolean = false
    @SerializedName("LoginStyleImage")
    var loginStyleImage: String? = null
    @SerializedName("AppStyleImage")
    var appStyleImage: String? = null
    @SerializedName("HelpdeskStyleImage")
    var helpdeskStyleImage: String? = null
    @SerializedName("HelpdeskThreadAction")
    var helpdeskThreadAction: String? = null
    @SerializedName("Default")
    var default: Long = 0
        private set
    /**
     * AllowUsersChangeInterfaceSettings : true
     * AllowUsersChangeEmailSettings : true
     * AllowUsersAddNewAccounts : true
     * AllowOpenPGP : true
     * AllowWebMail : true
     * DefaultTab : mailbox
     * AllowIosProfile : true
     * PasswordMinLength : 0
     * PasswordMustBeComplex : false
     * AllowRegistration : false
     * AllowPasswordReset : false
     * RegistrationDomains : []
     * RegistrationQuestions : []
     * SiteName : AfterLogic WebMail Pro
     * DefaultLanguage : English
     * DefaultLanguageShort : en
     * DefaultTheme : Default
     * Languages : [{"name":"English","value":"English"},{"name":"فارسی","value":"Persian"},{"name":"Română","value":"Romanian"},{"name":"Português Brasileiro","value":"Portuguese-Brazil"},{"name":"Українська","value":"Ukrainian"},{"name":"eesti","value":"Estonian"},{"name":"Русский","value":"Russian"},{"name":"Lietuvių","value":"Lithuanian"},{"name":"tiếng Việt","value":"Vietnamese"},{"name":"Italiano","value":"Italian"},{"name":"Français","value":"French"},{"name":"العربية","value":"Arabic"},{"name":"Norsk","value":"Norwegian"},{"name":"中文(香港)","value":"Chinese-Traditional"},{"name":"Suomi","value":"Finnish"},{"name":"Nederlands","value":"Dutch"},{"name":"עברית","value":"Hebrew"},{"name":"ภาษาไทย","value":"Thai"},{"name":"Svenska","value":"Swedish"},{"name":"Slovenščina","value":"Slovenian"},{"name":"日本語","value":"Japanese"},{"name":"Magyar","value":"Hungarian"},{"name":"Español","value":"Spanish"},{"name":"Ελληνικά","value":"Greek"},{"name":"Български","value":"Bulgarian"},{"name":"Türkçe","value":"Turkish"},{"name":"Polski","value":"Polish"},{"name":"中文(简体)","value":"Chinese-Simplified"},{"name":"Deutsch","value":"German"},{"name":"Čeština","value":"Czech"},{"name":"Latviešu","value":"Latvian"},{"name":"Português","value":"Portuguese-Portuguese"},{"name":"한국어","value":"Korean"},{"name":"Srpski","value":"Serbian"},{"name":"Dansk","value":"Danish"}]
     * Themes : ["Default","White","Blue","DeepForest","OpenWater","Autumn","BlueJeans","Ecloud","Funny"]
     * DateFormats : ["MM/DD/YYYY","DD/MM/YYYY","DD Month YYYY"]
     * AttachmentSizeLimit : 0
     * ImageUploadSizeLimit : 0
     * FileSizeLimit : 0
     * AutoSave : true
     * IdleSessionTimeout : 0
     * AllowInsertImage : true
     * AllowBodySize : false
     * MaxBodySize : 600
     * MaxSubjectSize : 255
     * JoinReplyPrefixes : true
     * AllowAppRegisterMailto : true
     * AllowPrefetch : true
     * AllowLanguageOnLogin : true
     * FlagsLangSelect : true
     * LoginFormType : 0
     * LoginSignMeType : 0
     * LoginAtDomainValue :
     * DemoWebMail : false
     * DemoWebMailLogin :
     * DemoWebMailPassword :
     * GoogleAnalyticsAccount :
     * CustomLoginUrl :
     * CustomLogoutUrl :
     * ShowQuotaBar : true
     * ServerUseUrlRewrite : true
     * ServerUrlRewriteBase : https://p7.afterlogic.com/
     * IosDetectOnLogin : true
     * AllowContactsSharing : true
     * LoginDescription :
     */

    @SerializedName("App")
    var app: App? = null
    /**
     * AccountID : 49
     * Email : test@afterlogic.com
     * FriendlyName : Alex Orlov
     * Signature : {"Signature":"<div data-crea=\"font-wrapper\" style=\"font-family: Tahoma; font-size: 16px; direction: ltr\></div>">/ Alex<br></br><\/div>","Type":1,"Options":1}
     * IsPasswordSpecified : true
     * AllowMail : true
     */

    @SerializedName("Accounts")
    var accounts: List<Account>? = null

    constructor() {}

    constructor(token: String) {
        this.token = token
    }

    fun setAuth(auth: Boolean) {
        isAuthorized = auth
    }

    fun setDefault(def: Int) {
        default = def.toLong()
    }


    class User {
        @SerializedName("IdUser")
        var idUser: Long = 0
            private set
        @SerializedName("MailsPerPage")
        var mailsPerPage: Int = 0
        @SerializedName("ContactsPerPage")
        var contactsPerPage: Int = 0
        @SerializedName("AutoCheckMailInterval")
        var autoCheckMailInterval: Int = 0
        @SerializedName("DefaultEditor")
        var defaultEditor: Int = 0
        @SerializedName("Layout")
        var layout: Int = 0
        @SerializedName("LoginsCount")
        var loginsCount: Int = 0
        @SerializedName("CanLoginWithPassword")
        var isCanLoginWithPassword: Boolean = false
        @SerializedName("DefaultTheme")
        var defaultTheme: String? = null
        @SerializedName("DefaultLanguage")
        var defaultLanguage: String? = null
        @SerializedName("DefaultLanguageShort")
        var defaultLanguageShort: String? = null
        @SerializedName("DefaultDateFormat")
        var defaultDateFormat: String? = null
        @SerializedName("DefaultTimeFormat")
        var defaultTimeFormat: Int = 0
        @SerializedName("DefaultTimeZone")
        var defaultTimeZone: String? = null
        @SerializedName("AllowCompose")
        var isAllowCompose: Boolean = false
        @SerializedName("AllowReply")
        var isAllowReply: Boolean = false
        @SerializedName("AllowForward")
        var isAllowForward: Boolean = false
        @SerializedName("AllowFetcher")
        var isAllowFetcher: Boolean = false
        @SerializedName("SaveMail")
        var saveMail: Int = 0
        @SerializedName("ThreadsEnabled")
        var isThreadsEnabled: Boolean = false
        @SerializedName("UseThreads")
        var isUseThreads: Boolean = false
        @SerializedName("SaveRepliedMessagesToCurrentFolder")
        var isSaveRepliedMessagesToCurrentFolder: Boolean = false
        @SerializedName("DesktopNotifications")
        var isDesktopNotifications: Boolean = false
        @SerializedName("AllowChangeInputDirection")
        var isAllowChangeInputDirection: Boolean = false
        @SerializedName("EnableOpenPgp")
        var isEnableOpenPgp: Boolean = false
        @SerializedName("AllowAutosaveInDrafts")
        var isAllowAutosaveInDrafts: Boolean = false
        @SerializedName("AutosignOutgoingEmails")
        var isAutosignOutgoingEmails: Boolean = false
        @SerializedName("EmailNotification")
        var emailNotification: String? = null
        @SerializedName("OutlookSyncEnable")
        var isOutlookSyncEnable: Boolean = false
        @SerializedName("MobileSyncEnable")
        var isMobileSyncEnable: Boolean = false
        @SerializedName("ShowPersonalContacts")
        var isShowPersonalContacts: Boolean = false
        @SerializedName("ShowGlobalContacts")
        var isShowGlobalContacts: Boolean = false
        @SerializedName("IsCollaborationSupported")
        var isIsCollaborationSupported: Boolean = false
        @SerializedName("AllowFilesSharing")
        var isAllowFilesSharing: Boolean = false
        @SerializedName("IsFilesSupported")
        var isIsFilesSupported: Boolean = false
        @SerializedName("IsHelpdeskSupported")
        var isIsHelpdeskSupported: Boolean = false
        @SerializedName("IsHelpdeskAgent")
        var isIsHelpdeskAgent: Boolean = false
        @SerializedName("AllowHelpdeskNotifications")
        var isAllowHelpdeskNotifications: Boolean = false
        @SerializedName("HelpdeskSignature")
        var helpdeskSignature: String? = null
        @SerializedName("HelpdeskSignatureEnable")
        var isHelpdeskSignatureEnable: Boolean = false
        @SerializedName("LastLoggedHost")
        var lastLogin: Int = 0
        @SerializedName("AllowVoice")
        var isAllowVoice: Boolean = false
        @SerializedName("VoiceProvider")
        var voiceProvider: String? = null
        @SerializedName("SipRealm")
        var sipRealm: String? = null
        @SerializedName("SipWebsocketProxyUrl")
        var sipWebsocketProxyUrl: String? = null
        @SerializedName("SipOutboundProxyUrl")
        var sipOutboundProxyUrl: String? = null
        @SerializedName("SipCallerID")
        var sipCallerID: String? = null
        @SerializedName("TwilioNumber")
        var twilioNumber: String? = null
        @SerializedName("TwilioEnable")
        var isTwilioEnable: Boolean = false
        @SerializedName("SipEnable")
        var isSipEnable: Boolean = false
        @SerializedName("SipImpi")
        var sipImpi: String? = null
        @SerializedName("SipPassword")
        var sipPassword: String? = null
        @SerializedName("FilesEnable")
        var isFilesEnable: Boolean = false
        @SerializedName("AllowCalendar")
        var isAllowCalendar: Boolean = false
        /**
         * ShowWeekEnds : false
         * ShowWorkDay : true
         * WorkDayStarts : 9
         * WorkDayEnds : 18
         * WeekStartsOn : 1
         * DefaultTab : 3
         * SyncLogin : test@afterlogic.com
         * DavServerUrl : https://p7-dav.afterlogic.com
         * DavPrincipalUrl : https://p7-dav.afterlogic.com/principals/test@afterlogic.com
         * AllowReminders : true
         */

        @SerializedName("Calendar")
        var calendar: Calendar? = null
        @SerializedName("CalendarSharing")
        var isCalendarSharing: Boolean = false
        @SerializedName("CalendarAppointments")
        var isCalendarAppointments: Boolean = false
        @SerializedName("IsDemo")
        var isIsDemo: Boolean = false

        fun setIdUser(idUser: Int) {
            this.idUser = idUser.toLong()
        }

        class Calendar {
            @SerializedName("ShowWeekEnds")
            var isShowWeekEnds: Boolean = false
            @SerializedName("ShowWorkDay")
            var isShowWorkDay: Boolean = false
            @SerializedName("WorkDayStarts")
            var workDayStarts: Int = 0
            @SerializedName("WorkDayEnds")
            var workDayEnds: Int = 0
            @SerializedName("WeekStartsOn")
            var weekStartsOn: Int = 0
            @SerializedName("DefaultTab")
            var defaultTab: Int = 0
            @SerializedName("SyncLogin")
            var syncLogin: String? = null
            @SerializedName("DavServerUrl")
            var davServerUrl: String? = null
            @SerializedName("DavPrincipalUrl")
            var davPrincipalUrl: String? = null
            @SerializedName("AllowReminders")
            var isAllowReminders: Boolean = false
        }
    }

    class Links {
        @SerializedName("ImportingContacts")
        var importingContacts: String? = null
        @SerializedName("OutlookSyncPlugin32")
        var outlookSyncPlugin32: String? = null
        @SerializedName("OutlookSyncPlugin64")
        var outlookSyncPlugin64: String? = null
        @SerializedName("OutlookSyncPluginReadMore")
        var outlookSyncPluginReadMore: String? = null
    }

    class Plugins {
        @SerializedName("ExternalServices")
        var externalServices: ExternalServices? = null

        class ExternalServices {
            /**
             * @Object : Object/CTenantSocials
             * Id : 631514845250-lsi1lf1j7vqsb5rq18lh4t499glp7f8b.apps.googleusercontent.com
             * Name : Google
             * LowerName : google
             * Allow : true
             * Scopes : ["auth","filestorage"]
             */

            @SerializedName("Connectors")
            var connectors: List<Connectors>? = null

            class Connectors {
                @SerializedName("Id")
                var id: String? = null
                @SerializedName("Name")
                var name: String? = null
                @SerializedName("LowerName")
                var lowerName: String? = null
                @SerializedName("Allow")
                var isAllow: Boolean = false
                @SerializedName("Scopes")
                var scopes: List<String>? = null
            }
        }
    }

    class App {
        @SerializedName("AllowUsersChangeInterfaceSettings")
        var isAllowUsersChangeInterfaceSettings: Boolean = false
        @SerializedName("AllowUsersChangeEmailSettings")
        var isAllowUsersChangeEmailSettings: Boolean = false
        @SerializedName("AllowUsersAddNewAccounts")
        var isAllowUsersAddNewAccounts: Boolean = false
        @SerializedName("AllowOpenPGP")
        var isAllowOpenPGP: Boolean = false
        @SerializedName("AllowWebMail")
        var isAllowWebMail: Boolean = false
        @SerializedName("DefaultTab")
        var defaultTab: String? = null
        @SerializedName("AllowIosProfile")
        var isAllowIosProfile: Boolean = false
        @SerializedName("PasswordMinLength")
        var passwordMinLength: Int = 0
        @SerializedName("PasswordMustBeComplex")
        var isPasswordMustBeComplex: Boolean = false
        @SerializedName("AllowRegistration")
        var isAllowRegistration: Boolean = false
        @SerializedName("AllowPasswordReset")
        var isAllowPasswordReset: Boolean = false
        @SerializedName("SiteName")
        var siteName: String? = null
        @SerializedName("DefaultLanguage")
        var defaultLanguage: String? = null
        @SerializedName("DefaultLanguageShort")
        var defaultLanguageShort: String? = null
        @SerializedName("DefaultTheme")
        var defaultTheme: String? = null
        @SerializedName("AttachmentSizeLimit")
        var attachmentSizeLimit: Int = 0
        @SerializedName("ImageUploadSizeLimit")
        var imageUploadSizeLimit: Int = 0
        @SerializedName("FileSizeLimit")
        var fileSizeLimit: Int = 0
        @SerializedName("AutoSave")
        var isAutoSave: Boolean = false
        @SerializedName("IdleSessionTimeout")
        var idleSessionTimeout: Int = 0
        @SerializedName("AllowInsertImage")
        var isAllowInsertImage: Boolean = false
        @SerializedName("AllowBodySize")
        var isAllowBodySize: Boolean = false
        @SerializedName("MaxBodySize")
        var maxBodySize: Int = 0
        @SerializedName("MaxSubjectSize")
        var maxSubjectSize: Int = 0
        @SerializedName("JoinReplyPrefixes")
        var isJoinReplyPrefixes: Boolean = false
        @SerializedName("AllowAppRegisterMailto")
        var isAllowAppRegisterMailto: Boolean = false
        @SerializedName("AllowPrefetch")
        var isAllowPrefetch: Boolean = false
        @SerializedName("AllowLanguageOnLogin")
        var isAllowLanguageOnLogin: Boolean = false
        @SerializedName("FlagsLangSelect")
        var isFlagsLangSelect: Boolean = false
        @SerializedName("LoginFormType")
        var loginFormType: Int = 0
        @SerializedName("LoginSignMeType")
        var loginSignMeType: Int = 0
        @SerializedName("LoginAtDomainValue")
        var loginAtDomainValue: String? = null
        @SerializedName("DemoWebMail")
        var isDemoWebMail: Boolean = false
        @SerializedName("DemoWebMailLogin")
        var demoWebMailLogin: String? = null
        @SerializedName("DemoWebMailPassword")
        var demoWebMailPassword: String? = null
        @SerializedName("GoogleAnalyticsAccount")
        var googleAnalyticsAccount: String? = null
        @SerializedName("CustomLoginUrl")
        var customLoginUrl: String? = null
        @SerializedName("CustomLogoutUrl")
        var customLogoutUrl: String? = null
        @SerializedName("ShowQuotaBar")
        var isShowQuotaBar: Boolean = false
        @SerializedName("ServerUseUrlRewrite")
        var isServerUseUrlRewrite: Boolean = false
        @SerializedName("ServerUrlRewriteBase")
        var serverUrlRewriteBase: String? = null
        @SerializedName("IosDetectOnLogin")
        var isIosDetectOnLogin: Boolean = false
        @SerializedName("AllowContactsSharing")
        var isAllowContactsSharing: Boolean = false
        @SerializedName("LoginDescription")
        var loginDescription: String? = null
        @SerializedName("RegistrationDomains")
        var registrationDomains: List<*>? = null
        @SerializedName("RegistrationQuestions")
        var registrationQuestions: List<*>? = null

        @SerializedName("AllowExternalClientCustomAuthentication")
        val isAllowExternalClientCustomAuthentication: Boolean = false

        /**
         * name : English
         * value : English
         */

        @SerializedName("Languages")
        var languages: List<Languages>? = null
        @SerializedName("Themes")
        var themes: List<String>? = null
        @SerializedName("DateFormats")
        var dateFormats: List<String>? = null

        class Languages {
            @SerializedName("name")
            var name: String? = null
            @SerializedName("value")
            var value: String? = null
        }
    }

    class Account {
        @SerializedName("AccountID")
        var accountID: Long = 0
            private set
        @SerializedName("Email")
        var email: String? = null
        @SerializedName("FriendlyName")
        var friendlyName: String? = null
        /**
         * Signature : <div data-crea="font-wrapper" style="font-family: Tahoma; font-size: 16px; direction: ltr">/ Alex<br></br></div>
         * Type : 1
         * Options : 1
         */

        @SerializedName("Signature")
        var signature: Signature? = null
        @SerializedName("IsPasswordSpecified")
        var isIsPasswordSpecified: Boolean = false
        @SerializedName("AllowMail")
        var isAllowMail: Boolean = false

        fun setAccountID(accountID: Int) {
            this.accountID = accountID.toLong()
        }

        class Signature {
            @SerializedName("Signature")
            var signature: String? = null
            @SerializedName("Type")
            var type: Int = 0
            @SerializedName("Options")
            var options: Int = 0
        }
    }
}
