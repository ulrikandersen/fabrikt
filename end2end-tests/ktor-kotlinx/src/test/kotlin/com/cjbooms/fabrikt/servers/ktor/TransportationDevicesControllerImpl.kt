package com.cjbooms.fabrikt.servers.ktor

import com.example.controllers.TransportationDevicesController
import com.example.controllers.TypedApplicationCall
import com.example.models.TransportationDevice
import com.example.models.TransportationDeviceDeviceType
import io.mockk.CapturingSlot

class TransportationDevicesControllerImpl(
    private val bodyCapturingSlot: CapturingSlot<TransportationDevice>,
) : TransportationDevicesController {

    override suspend fun listTransportationDevices(call: TypedApplicationCall<List<TransportationDevice>>) {
        call.respondTyped(listOf(
            TransportationDevice(
                deviceType = TransportationDeviceDeviceType.ROLLERSKATES,
                make = "Roller Master",
                model = "Pro"
            ),
            TransportationDevice(
                deviceType = TransportationDeviceDeviceType.BIKE,
                make = "Bike Co",
                model = "Mountain Goat"
            )
        ))
    }

    override suspend fun createTransportationDevice(
        transportationDevice: TransportationDevice,
        call: TypedApplicationCall<TransportationDevice>
    ) {
        bodyCapturingSlot.captured = transportationDevice
        call.respondTyped(transportationDevice)
    }
}
