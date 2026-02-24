package com.zonezapapp.api

import com.zonezapapp.BuildConfig

object ApiClient {
    private var _api: ZoneZapApi? = null
    private var _context: android.content.Context? = null

    fun init(context: android.content.Context) {
        _context = context.applicationContext
        AuthManager.init(_context!!)
        _api = ZoneZapApi.create(_context!!, BuildConfig.API_BASE_URL)
    }

    fun api(): ZoneZapApi {
        if (_api == null && _context != null) _api = ZoneZapApi.create(_context!!, BuildConfig.API_BASE_URL)
        return _api ?: throw IllegalStateException("Call ApiClient.init(context) first (e.g. in Application or LoginActivity)")
    }
}
