package com.example.skincancerdetectormni.models

import kotlinx.serialization.Serializable


data class ModelResponse(
    val diagnosis: Diagnosis,
    val interpretation: String
)