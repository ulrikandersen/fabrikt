package com.cjbooms.fabrikt.servers.ktor

import com.example.multipart.controllers.FilesUploadController
import com.example.multipart.controllers.FilesUploadMultipleController
import com.example.multipart.controllers.ReceivedFile
import com.example.multipart.controllers.TypedApplicationCall
import com.example.multipart.models.UploadResponse
import io.mockk.CapturingSlot

class FilesUploadControllerImpl(
    private val fileSlot: CapturingSlot<ReceivedFile>,
) : FilesUploadController {
    override suspend fun uploadFile(file: ReceivedFile, call: TypedApplicationCall<UploadResponse>) {
        fileSlot.captured = file
        call.respondTyped(
            UploadResponse(id = "file-123")
        )
    }
}

class FilesUploadMultipleControllerImpl(
    private val filesSlot: CapturingSlot<List<ReceivedFile>>,
) : FilesUploadMultipleController {
    override suspend fun uploadMultipleFiles(files: List<ReceivedFile>, call: TypedApplicationCall<UploadResponse>) {
        filesSlot.captured = files
        call.respondTyped(
            UploadResponse(id = "file-multi")
        )
    }
}
