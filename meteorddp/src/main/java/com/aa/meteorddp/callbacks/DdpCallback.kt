package com.aa.meteorddp.callbacks

interface DdpCallback {

    fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?)

    fun onDataChanged(
        collectionName: String?, documentID: String?,
        updatedValuesJson: String?,
        removedValuesJson: String?
    )

    fun onDataRemoved(collectionName: String?, documentID: String?)
}
