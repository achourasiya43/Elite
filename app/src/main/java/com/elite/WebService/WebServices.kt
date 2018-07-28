package com.elite.WebService

/**
 * Created by anil on 8/11/17.
 */

object WebServices {
   // private val Base_Url = "http://gnmtechnology.com/elite/"
    private val Base_Url = "http://elitedentalsolutions.com/"

    val Registration_Url = Base_Url+"service/userRegistration"
    val Login_Url =  Base_Url+"service/userLogin"
    val Forgot_Password =  Base_Url+"service/forgotPassword"
    val Category_url =  Base_Url+"service/categoryList"
    val Update_Doctor_profile =  Base_Url+"service/user/updateDoctorProfile"
    val Update_Agency_profile =  Base_Url+"service/user/updateAgencyProfile"
    val ChangePassword =  Base_Url+"service/user/changePassword"
    val PostNewVacancy =  Base_Url+"service/user/addPostNewVacancy"
    val JobAllList =  Base_Url+"service/jobList"
    val UpdateJob =  Base_Url+"service/user/jobEdit"
    val AllRequest =  Base_Url+"service/doctorToAgecyRequest"
    val ShowDrDetails =  Base_Url+"service/user/showDrDetails"
    val InterestedAgencyForFinish =  Base_Url+"service/user/interestedAgencyTojobs"
    val Delete_Post =  Base_Url+"service/user/deletePost"
    val InterestedAgency =  Base_Url+"service/user/getInterestedAgency"
    val AllDrList =  Base_Url+"service/user/showAllDoctorVacancyList"
    val DrDetails =  Base_Url+"service/user/showDoctorIndivisualVacancy"
    val FinishJob =  Base_Url+"service/user/finishJob"
    val AgencySideInterested =  Base_Url+"service/user/doctorListInterestedByAgency"
    val AgencySideFinished =  Base_Url+"service/user/finishedJobListAgencySide"
    val DoctorSideFinished =  Base_Url+"service/user/doctorFinishedJobList"
    val LogoutApp_URL = Base_Url+"service/user/logout"
    val NotificationList = Base_Url+"service/user/notificationList"
    val NotificationOnOff = Base_Url+"service/user/notificationOnOff"
    val AboutUs = Base_Url+"service/page_content"

}
