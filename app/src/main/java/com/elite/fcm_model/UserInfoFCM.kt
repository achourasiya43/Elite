package com.elite.fcm_model

import java.io.Serializable

/**
 * Created by mindiii on 6/9/17.
 */
class UserInfoFCM :Serializable {

    var uid :String = ""
    var email:String = ""
    var name :String = ""
    var firebaseToken:String = ""
    var profilePic:String = ""
    var userType :String = ""
    var firebaseId :String = ""

  init {
      this.uid
      this.email
      this.name
      this.firebaseToken
      this.profilePic
      this.userType
      this.firebaseId
  }


}
