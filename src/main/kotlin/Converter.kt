import java.io.File
import java.nio.charset.Charset

/**
 * ATOKのローマ字マップをGoogle日本語入力用に変換するクラス
 */
class Converter {
    /**
     * 変換処理の実行
     */
    fun convert(file: File): String {
        // 受け取ったファイルインスタンスを読み込み、テキストを取得
        // NOTE: 上から4行は不要なので削除
        val lines = file.readText(Charset.forName("Shift_JIS")).lines().drop(4).toMutableList()

        // 連続文字（bb, ccなど）のリスト
        // NOTE: これはGoogle日本語入力のローマ字マップにおける「次の入力」に文字を入れる必要があるかどうかを判定するために使用する。
        val continuous = mutableListOf<String>()

        // 1行ずつ変換処理を実行
        lines.forEachIndexed { index, line ->
            // 空行ならスキップする。
            if (line.isEmpty()) {
                return@forEachIndexed
            }

            // 空白区切りで2列取得
            val columns = line.split("\\s+".toRegex())

            // 1列目：Google日本語入力のローマ字マップでは半角である必要がある。
            val first = columns[0].toHalfWidth()

            // 2列目：基本的にはそのまま取得
            val second = columns[1]

            if (isContinuousChar(first)) {
                continuous.add(first)
            }

            // Google日本語入力では各列の区切りはタブ文字
            val result = "$first\t$second\t"

            lines[index] = result
        }
        addThirdColumnIfRequired(lines, continuous)

        // 変換結果をString型で返す。
        return lines.joinToString("\n")
    }

    /**
     * 全角文字を半角文字に変換する。
     * TODO: Copilotによる自動生成なので間違っているかもしれない。
     */
    private fun String.toHalfWidth(): String {
        // 文字列を1文字ずつに分割し、それぞれを変換
        return this.map { c ->
            // 文字コードを取得
            val code = c.toInt()
            // 全角文字の場合
            if (code in 0xFF01..0xFF5E) {
                // 文字コードの差分を取得
                val diff = 0xFEE0
                // 文字コードを差分分減算し、文字に変換
                (code - diff).toChar()
            } else {
                // そのまま返す
                c
            }
        }.joinToString("")
    }

    /**
     * 3列目（=次の入力）を追加する必要がある場合は追加する。
     *
     * Google日本語入力の場合、1列目、2列めはATOKと同じ（入力と出力）だが3列目(==次の入力)が存在する。
     * 特定の入力・出力の組み合わせのときはローマ字マップに従って変換した後に指定した文字列を自動で出せる機能
     * ATOKにはわざわざマップで登録しなくてもデフォルトで備わっているが、Google日本語入力では内部的にマッピングしているらしい。
     * 例）bb -> っb, cc -> っc
     * そのため、ATOKで連続文字(bbなど)のマッピングがない場合は追加する必要がある。
     */
    private fun addThirdColumnIfRequired(
        lines: MutableList<String>,
        continuous: MutableList<String>
    ) {

        ContinuousString.values().forEach {
            // 既に登録されている連続文字の場合、何もしない。
            if (continuous.contains(it.name)) {
                return@forEach
            }

            // 連続文字の場合、3列目に次の入力を追加する。
            // ローマ字マップの末端への追加で問題ない（順番を考慮する必要はなく、インポート時にGoogle日本語入力の方で解決してくれる）
            val str = "${it.name}\tっ\t${getNextInputCharIfRequired(it.name)}"
            lines.add(str)
        }
    }

    /**
     * 渡された文字列が連続する文字、かつATOKとGoogle日本語入力の互換性を保つため対応する必要がある文字列であるならばtrueを返す。
     */
    private fun isContinuousChar(input: String): Boolean {
        when (input) {
            "bb",
            "cc",
            "dd",
            "ff",
            "gg",
            "hh",
            "jj",
            "kk",
            "ll",
            "mm",
            "pp",
            "qq",
            "rr",
            "ss",
            "tt",
            "vv",
            "ww",
            "xx",
            "yy",
            "zz" -> return true

            else -> return false
        }
    }

    /**
     * Google日本語入力のローマ字マップにおける「次の入力」に文字を入れる必要があるかどうかを判定し、必要ならば次の入力を返す。
     * 不要なら空白文字を返す。
     */
    private fun getNextInputCharIfRequired(input: String): Char {
        when (input) {
            "bb" -> return 'b'
            "cc" -> return 'c'
            "dd" -> return 'd'
            "ff" -> return 'f'
            "gg" -> return 'g'
            "hh" -> return 'h'
            "jj" -> return 'j'
            "kk" -> return 'k'
            "ll" -> return 'l'
            "mm" -> return 'm'
            "pp" -> return 'p'
            "qq" -> return 'q'
            "rr" -> return 'r'
            "ss" -> return 's'
            "tt" -> return 't'
            "vv" -> return 'v'
            "ww" -> return 'w'
            "xx" -> return 'x'
            "yy" -> return 'y'
            "zz" -> return 'z'
        }
        return ' '
    }

    /**
     * ATOKとGoogle日本語入力の互換性担保のために必要な連続文字(小文字)のリスト
     */
    private enum class ContinuousString {
        // enumのnameを使用したいので意図的に小文字で定義
        bb,
        cc,
        dd,
        ff,
        gg,
        hh,
        jj,
        kk,
        ll,
        mm,
        pp,
        qq,
        rr,
        ss,
        tt,
        vv,
        ww,
        xx,
        yy,
        zz
    }
}