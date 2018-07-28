package com.elite.model

import java.io.Serializable

/**
 * Created by anil on 22/11/17.
 */
class DrPostDetails {
    var status: String? = null
    var message: String? = null
    var postList: List<PostListBean>? = null


    class PostListBean {

        var id: String? = null
        var job_title: String? = null
        var job_type: String? = null
        var vacancy_number: String? = null
        var qualification: String? = null
        var need_from_date: String? = null
        var job_timing: String? = null
        var salary: String? = null
        var description: String? = null
        var location: String? = null
        var finished_by: String? = null
        var crd: String? = null
        var upd: String? = null
        var latitude: String? = null
        var longitude: Any? = null
        var CatId: String? = null
        var interested_count: String? = null

    }

}