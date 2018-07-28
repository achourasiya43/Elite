package com.elite.model.doctor_side

/**
 * Created by anil on 29/11/17.
 */
class DrListInfo {

    var status: String? = null
    var message: String? = null
    var allDoctorVacancyList: ArrayList<AllDoctorVacancyListBean> = ArrayList()

    class AllDoctorVacancyListBean {

        var id: String? = null
        var job_title: String? = null
        var qualification: String? = null
        var vacancy_number: String? = null
        var salary: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var CatId: String? = null
        var crd: String? = null
        var upd: String? = null
        var interested: String? = null
        var day: String? = null

    }
}