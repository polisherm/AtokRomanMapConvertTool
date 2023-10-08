import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane

/**
 * ATOKでエクスポートしたローマ字マップをGoogle日本語入力用に書き換えるツール
 * 書き換えたテキストファイルはユーザーが指定した任意のパスに出力される。
 * そのテキストファイルをGoogle日本語入力のローマ字設定画面からインポートすることで、インポートが完了する。
 */
class ConvertAppFrame : JFrame() {
    private val openButton = JButton("開く").apply {
        addActionListener {
            // ファイル選択ダイアログを表示
            val fileChooser = JFileChooser()
            val selected = fileChooser.showOpenDialog(this@ConvertAppFrame)
            if (selected == JFileChooser.APPROVE_OPTION) {
                // 選択されたファイルを取得
                val file = fileChooser.selectedFile

                // ファイルを読み込み、変換処理を実行
                val converted = Converter().convert(file)

                // ファイル保存ダイアログを表示
                val saveFileChooser = object : JFileChooser() {
                    override fun approveSelection() {
                        // もし拡張子なしでパスが指定されたらここで拡張子（.txt）を付与する。
                        if (selectedFile.extension.isEmpty()) {
                            selectedFile = File("${selectedFile.absolutePath}.txt")
                        }

                        if (selectedFile.exists() && dialogType == SAVE_DIALOG) {
                            val result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION)
                            when (result) {
                                JOptionPane.YES_OPTION -> super.approveSelection()
                                JOptionPane.NO_OPTION -> return
                                JOptionPane.CLOSED_OPTION -> return
                                JOptionPane.CANCEL_OPTION -> cancelSelection()
                            }
                        }

                        super.approveSelection()
                    }
                }
                val saveSelected = saveFileChooser.showSaveDialog(this@ConvertAppFrame)
                if (saveSelected == JFileChooser.APPROVE_OPTION) {
                    val saveFile = saveFileChooser.selectedFile
                    saveFile.writeText(converted)
                }
            }
        }
    }

    init {
        // メインフレームの設定
        title = "ATOKローマ字マップ変換ツール"
        setSize(400, 300)
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE

        add(openButton)
    }
}