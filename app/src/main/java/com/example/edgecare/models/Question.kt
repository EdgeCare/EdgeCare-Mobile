package com.example.edgecare.models

import android.os.Parcel
import android.os.Parcelable

data class Question(
    val questionText: String,
    val inputType: String,
    val explanation: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(questionText)
        parcel.writeString(inputType)
        parcel.writeString(explanation)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Question> {
        override fun createFromParcel(parcel: Parcel): Question {
            return Question(
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<Question?> = arrayOfNulls(size)
    }
}
