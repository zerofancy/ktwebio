package top.ntutn.ktwebio

class TextInputContent(key: String, private val label: String, private val value: String): InputContent() {
    private val fieldName: String = key

    override fun getHtml(): String = """
        <div class="mb-3">
          <label for="$fieldName" class="form-label">$label</label>
          <input class="form-control" id="$fieldName" placeholder="" value="$value">
        </div>
    """.trimIndent()

    override fun readValueJs(): String = """
        formData.append("$fieldName", document.getElementById("$fieldName").value)
    """.trimIndent()
}