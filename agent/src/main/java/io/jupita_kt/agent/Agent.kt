package io.jupita_kt.agent

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.lang.Exception

class Agent(
    context: Context,
    private val apiKey: String,
    private val agentId: String
    ){

    private val requestQueue = Volley.newRequestQueue(context)
    private fun dumpRequestAPI(text: String, clientId: String, type: Int, isCall: Boolean, dumpListener: DumpListener?){
        val jsonData = JSONObject(mapOf(
            "token" to apiKey,
            "agent id" to agentId,
            "client id" to clientId,
            "message type" to type,
            "text" to text,
            "isCall" to isCall
        ))

        val request = JsonObjectRequest(
            Constants.dumpEndpoint,
            jsonData,
            {
                try{
                    dumpListener?.onSuccess(
                        it.getString("message"),
                        it.getDouble("score")
                    )
                } catch (e: JSONException){
                    Log.e(TAG, e.message, e)
                    e.printStackTrace()
                }
            }
        ) {
            val body: String
            var jsonResponse = JSONObject()
            val statusCode = it.networkResponse.statusCode.toString()

            if (it.networkResponse.data != null){
                try {
                    body = it.networkResponse.data.decodeToString()
                    jsonResponse = JSONObject(body)
                } catch (e: UnsupportedEncodingException){
                    Log.e(TAG, e.message, e)
                } catch (e: JSONException){
                    Log.e(TAG, e.message, e)
                }
            }

            dumpListener?.onError(statusCode, jsonResponse)
        }

        requestQueue.add(request)
    }

    private fun feedRequestAPI(feedListener: FeedListener?){
        val jsonData = JSONObject(
            mapOf(
                "token" to apiKey,
                "agent id" to agentId
            )
        )

        val request = JsonObjectRequest(
            Constants.feedEndpoint,
            jsonData,
            { it?.let { feedListener?.onSuccess(it) }}
        ) {
            val body: String
            var jsonResponse = JSONObject()
            val statusCode = it.networkResponse.statusCode.toString()

            if (it.networkResponse.data != null){
                try {
                    body = it.networkResponse.data.decodeToString()
                    jsonResponse = JSONObject(body)
                } catch (e: UnsupportedEncodingException){
                    Log.e(TAG, e.message, e)
                } catch (e: JSONException){
                    Log.e(TAG, e.message, e)
                }
            }

            feedListener?.onError(statusCode, jsonResponse)
        }

        requestQueue.add(request)

    }

    private fun ratingRequestAPI(model: String, ratingListener: RatingListener?){
        val jsonData = JSONObject(
            mapOf(
                "token" to apiKey,
                "agent id" to agentId,
                "model" to model
            )
        )

        val request = JsonObjectRequest(
            Constants.ratingEndpoint,
            jsonData,
            {ratingListener?.onSuccess(it.getDouble("rating"))}
        ) {
            val body: String
            var jsonResponse = JSONObject()
            val statusCode = it.networkResponse.statusCode.toString()

            if (it.networkResponse.data != null){
                try {
                    body = it.networkResponse.data.decodeToString()
                    jsonResponse = JSONObject(body)
                } catch (e: UnsupportedEncodingException){
                    Log.e(TAG, e.message, e)
                } catch (e: JSONException){
                    Log.e(TAG, e.message, e)
                }
            }

            ratingListener?.onError(statusCode, jsonResponse)
        }

        requestQueue.add(request)
    }



    fun dump(text: String, clientId: String, type: Int, isCall: Boolean, dumpListener: DumpListener){
        dumpRequestAPI(text, clientId, type, isCall, dumpListener)
    }

    fun dump(text: String, clientId: String){
        dumpRequestAPI(text, clientId, MessageType.Agent, false, null)
    }

    fun dump(text: String, clientId: String, dumpListener: DumpListener){
        dumpRequestAPI(text, clientId, MessageType.Agent, false, dumpListener)
    }

    fun dump(text: String, clientId: String, type: Int, dumpListener: DumpListener){
        dumpRequestAPI(text, clientId, type, false, dumpListener)
    }

    fun feed(feedListener: FeedListener){
        feedRequestAPI(feedListener)
    }

    fun rating(ratingListener: RatingListener){
        ratingRequestAPI(ModelName.JUPITAV1, ratingListener)
    }

    fun rating(modelName: String, ratingListener: RatingListener){
        ratingRequestAPI(modelName, ratingListener)
    }

    // builder pattern
    class Builder(private val context: Context, private val apiKey: String){
        var agentId: String? = null

        fun build(): Agent{
            return agentId?.let { Agent(context, apiKey, it) } ?: throw Exception()
        }
    }

    // listeners
    interface DumpListener{
        fun onSuccess(msg: String, rating: Double)
        fun onError(statusCode: String, response: JSONObject)
    }

    interface FeedListener {
        fun onSuccess(week: JSONObject)
        fun onError(statusCode: String, response: JSONObject)
    }
    interface RatingListener {
        fun onSuccess(rating: Double)
        fun onError(statusCode: String, response: JSONObject)
    }

    companion object {
        private const val TAG = "AGENT"
    }
}