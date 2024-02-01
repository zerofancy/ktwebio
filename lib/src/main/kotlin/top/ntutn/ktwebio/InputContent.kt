package top.ntutn.ktwebio

abstract class InputContent: IWebIOContent {
    /**
     * @return add value to formData
     */
    open fun readValueJs() = "\"\""
}