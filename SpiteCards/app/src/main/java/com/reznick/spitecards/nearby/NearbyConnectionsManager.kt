package com.reznick.spitecards.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

private const val SERVICE_ID = "com.reznick.spitecards"

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Advertising : ConnectionState()
    object Discovering : ConnectionState()
    data class Connected(val endpointIds: List<String>) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class DiscoveredEndpoint(
    val id: String,
    val name: String
)

class NearbyConnectionsManager(private val context: Context) {

    private val client = Nearby.getConnectionsClient(context)

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<Pair<String, GameMessage>>(extraBufferCapacity = 64)
    val messages: SharedFlow<Pair<String, GameMessage>> = _messages.asSharedFlow()

    private val _discovered = MutableSharedFlow<DiscoveredEndpoint>(extraBufferCapacity = 16)
    val discovered: SharedFlow<DiscoveredEndpoint> = _discovered.asSharedFlow()

    private val connectedEndpoints = mutableSetOf<String>()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { bytes ->
                    runCatching { _messages.tryEmit(endpointId to bytes.decodeMessage()) }
                }
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpoints.add(endpointId)
                _state.value = ConnectionState.Connected(connectedEndpoints.toList())
            }
        }
        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            _state.value = if (connectedEndpoints.isEmpty()) ConnectionState.Idle
            else ConnectionState.Connected(connectedEndpoints.toList())
        }
    }

    fun startAdvertising(displayName: String) {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        client.startAdvertising(displayName, SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener { _state.value = ConnectionState.Advertising }
            .addOnFailureListener { _state.value = ConnectionState.Error(it.message ?: "Advertising failed") }
    }

    fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        client.startDiscovery(SERVICE_ID, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                _discovered.tryEmit(DiscoveredEndpoint(endpointId, info.endpointName))
            }
            override fun onEndpointLost(endpointId: String) {}
        }, options)
            .addOnSuccessListener { _state.value = ConnectionState.Discovering }
            .addOnFailureListener { _state.value = ConnectionState.Error(it.message ?: "Discovery failed") }
    }

    fun requestConnection(endpointId: String, localName: String) {
        client.requestConnection(localName, endpointId, connectionLifecycleCallback)
    }

    fun send(endpointId: String, message: GameMessage) {
        client.sendPayload(endpointId, Payload.fromBytes(message.encode()))
    }

    fun broadcast(message: GameMessage) {
        val payload = Payload.fromBytes(message.encode())
        connectedEndpoints.forEach { client.sendPayload(it, payload) }
    }

    fun stop() {
        client.stopAllEndpoints()
        connectedEndpoints.clear()
        _state.value = ConnectionState.Idle
    }
}
