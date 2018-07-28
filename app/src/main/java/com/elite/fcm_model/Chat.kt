package com.elite.fcm_model

import java.io.Serializable

/**
 * Created by anil on 12/12/17.
 */
class Chat : Serializable{

    var name: String = ""
    var firebaseId: String = ""
    var message: String = ""
    var timestamp: String = ""
    var deleteby: String = ""
    var firebaseToken: String = ""

    var category_Id: String = ""
    var category_name: String = ""
    var profilePic: String = ""
    var uid: String = ""



  init {

      this.uid
      this.name
      this.firebaseId
      this.message
      this.timestamp
      this.deleteby
      this.firebaseToken
      this.profilePic
      this.category_Id

  }
}