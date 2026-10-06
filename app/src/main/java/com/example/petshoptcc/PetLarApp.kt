package com.example.petshoptcc

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Liga o app ao mesmo projeto Firebase do site (petlar-263e9): as contas e os pedidos
 * ficam online e aparecem nos dois. Os valores vêm do google-services.json do app Android
 * "PetLar App" (Console do Firebase › Configurações do projeto › Seus apps); são públicos
 * por natureza, quem protege os dados são as regras do Firestore.
 */
class PetLarApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(
                this,
                FirebaseOptions.Builder()
                    .setApiKey("AIzaSyA4BMX4LSmJj9ay9umOnTc53gfi_FowDd8")
                    .setApplicationId("1:557381846758:android:4d89b033068e5a57101d27")
                    .setProjectId("petlar-263e9")
                    .setStorageBucket("petlar-263e9.firebasestorage.app")
                    .setGcmSenderId("557381846758")
                    .build()
            )
        }
    }
}
