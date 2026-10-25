package com.example.sentencecount.ui.components

object SampleTexts {
    val SAMPLES = listOf(
        SampleItem(
            name = "Classic Literature",
            text = """The quick brown fox jumps over the lazy dog. It runs across the field. What an amazing sight! Dr. Smith arrived early at 8.30 a.m. to observe the wildlife. "Are you coming?" he asked. Then he waited quietly (for nearly twenty minutes). He noticed version 2.0 of the tracker was online at https://nature-reserve.org/fox. Would you believe it?!"""
        ),
        SampleItem(
            name = "Dialogue & Quotes",
            text = """"Wait... Are you serious?" she asked. He nodded slowly. "I don't know what else to say." The room remained silent for several seconds. Outside, rain tapped softly against the window glass."""
        ),
        SampleItem(
            name = "Multilingual",
            text = """Hello world. Bonjour tout le monde ! Comment allez-vous ? ¡Qué bonito día hace hoy! 你好世界。今天好吗？ مرحبا بالعالم. كيف حالك؟"""
        ),
        SampleItem(
            name = "Analytical / Technical",
            text = """System throughput reached 1,250 requests per second. The average latency is 12.50 ms across all cluster nodes. Please email support@example.com for detailed diagnostics. Do not reboot server A.1 without authorization."""
        )
    )
}

data class SampleItem(
    val name: String,
    val text: String
)
