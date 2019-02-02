package org.mobiletoolkit.android.repository

/**
 * Created by Sebastian Owodzin on 02/12/2018.
 */
interface Model<Identifier> {

    fun _identifier(): Identifier?
}