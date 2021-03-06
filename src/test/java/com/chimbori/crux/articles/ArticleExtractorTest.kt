package com.chimbori.crux.articles

import com.chimbori.crux.extractFromTestFile
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleExtractorTest {
  @Test
  fun testRetainSpaceInsideTags() {
    val As = "aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa aaa"
    val Bs = "bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb"
    val Cs = "ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc ccc"
    var article = ArticleExtractor(EXAMPLE_URL, String.format("<html><body><div> %s <p> %s</p>%s </div></body></html>", As, Bs, Cs)).extractContent().article
    assertEquals(3, article.document?.childNodeSize())
    assertEquals(As, article.document?.childNode(0)?.outerHtml()?.trim { it <= ' ' })
    assertEquals(String.format("<p> %s</p>", Bs), article.document?.childNode(1)?.outerHtml()?.trim { it <= ' ' })
    assertEquals(Cs, article.document?.childNode(2)?.outerHtml()?.trim { it <= ' ' })
    article = ArticleExtractor(EXAMPLE_URL, String.format("<html><body><div> %s <p>%s </p>%s</div></body></html>", As, Bs, Cs)).extractContent().article
    assertEquals(3, article.document?.childNodeSize())
    assertEquals(As, article.document?.childNode(0)?.outerHtml()?.trim { it <= ' ' })
    assertEquals(String.format("<p>%s </p>", Bs), article.document?.childNode(1)?.outerHtml()?.trim { it <= ' ' })
    assertEquals(Cs, article.document?.childNode(2)?.outerHtml()?.trim { it <= ' ' })
    article = ArticleExtractor(EXAMPLE_URL, String.format("<html><body><div> %s <p> %s </p>%s</div></body></html>", As, Bs, Cs)).extractContent().article
    assertEquals(3, article.document?.childNodeSize())
    assertEquals(As, article.document?.childNode(0)?.outerHtml()?.trim { it <= ' ' })
    assertEquals(String.format("<p> %s </p>", Bs), article.document?.childNode(1)?.outerHtml()?.trim { it <= ' ' })
    assertEquals(Cs, article.document?.childNode(2)?.outerHtml()?.trim { it <= ' ' })
  }

  @Test
  fun testThatHiddenTextIsNotExtracted() {
    val article = ArticleExtractor(EXAMPLE_URL,
        """<div style="margin: 5px; display:none; padding: 5px;">
          |Hidden Text
          |</div>
          |<div style="margin: 5px; display:block; padding: 5px;">
          |Visible Text that has to be longer than X characters so it’s not stripped out for being too short.
          |</div>
          |<div>Default Text</div>
          |""".trimMargin()).extractContent().article
    assertEquals("Visible Text that has to be longer than X characters so it’s not stripped out for being too short.", article.document?.text())
  }

  @Test
  fun testThatLongerTextIsPreferred() {
    val article = ArticleExtractor(EXAMPLE_URL,
        """<div style="margin: 5px; display:none; padding: 5px;">
          |Hidden Text
          |</div>
          |<div style="margin: 5px; display:block; padding: 5px;">
          |Visible Text that’s still longer than our minimum text size limits
          |</div>
          |<div>
          |Default Text but longer that’s still longer than our minimum text size limits
          |</div>
          |""".trimMargin()).extractContent().article
    assertEquals("Default Text but longer that’s still longer than our minimum text size limits", article.document?.text())
  }

  @Test
  fun testThatShortTextIsDiscarded() {
    val article = ArticleExtractor(EXAMPLE_URL,
        """<div>
          |Default Text but longer that’s still longer than our minimum text size limits
          |<div>short text</div>
          |</div>""".trimMargin()).extractContent().article
    assertEquals("Default Text but longer that’s still longer than our minimum text size limits", article.document?.text())
  }

  @Test
  fun testThatImportantShortTextIsRetained() {
    val article = ArticleExtractor(EXAMPLE_URL,
        """<div>
          |Default Text but longer that’s still longer than our minimum text size limits
          |<div crux-keep>short text</div>
          |</div>""".trimMargin()).extractContent().article
    assertEquals("Default Text but longer that’s still longer than our minimum text size limits short text", article.document?.text())
  }

  @Test
  fun testReadingTimeEstimates() {
    val washingtonPostArticle = extractFromTestFile(
        "https://www.washingtonpost.com/lifestyle/style/the-nearly-forgotten-story-of-the-black-women-who-helped-land-a-man-on-the-moon/2016/09/12/95f2d356-7504-11e6-8149-b8d05321db62_story.html".toHttpUrlOrNull()!!,
        "washingtonpost.html")
    assertEquals(8, washingtonPostArticle!!.estimatedReadingTimeMinutes)
    val galileoArticle = extractFromTestFile(
        "https://en.wikipedia.org/wiki/Galileo_Galilei".toHttpUrlOrNull()!!, "wikipedia_galileo.html")
    assertEquals(53, galileoArticle!!.estimatedReadingTimeMinutes)
  }

  companion object {
    private val EXAMPLE_URL = "http://example.com/".toHttpUrlOrNull()!!
  }
}
