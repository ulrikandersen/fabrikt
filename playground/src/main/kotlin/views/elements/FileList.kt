import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.li
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.ul
import kotlinx.html.unsafe

fun FlowContent.fileList(files: List<Pair<String, String>>) {
    ul(classes = "file-nav") {
        files.forEach { (fileName, label) ->
            li {
                a(href = "#$fileName") {
                    +fileName
                    span(classes = "file-tag ${label.lowercase()}") { +label }
                }
            }
        }
    }
    script {
        unsafe {
            +"""
                document.querySelectorAll('.file-nav a').forEach(function(link) {
                    link.addEventListener('click', function(e) {
                        e.preventDefault();
                        var target = document.getElementById(this.getAttribute('href').slice(1));
                        var nav = document.querySelector('.file-nav');
                        var panel = nav && nav.closest('.panel');
                        if (target && nav && panel) {
                            var delta = target.getBoundingClientRect().top - nav.getBoundingClientRect().bottom;
                            panel.scrollBy({ top: delta });
                        }
                    });
                });
            """.trimIndent()
        }
    }
}
