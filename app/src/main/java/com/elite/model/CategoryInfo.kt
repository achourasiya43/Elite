package com.elite.model

/**
 * Created by Vicky on 12-Nov-17.
 */

class CategoryInfo {

    var status: String? = null
    var message: String? = null
    var allCategoryList: List<AllCategoryListBean>? = null

    class AllCategoryListBean {

        var id: String? = null
        var name: String? = null
        var ischecked:Boolean = false
    }
}
