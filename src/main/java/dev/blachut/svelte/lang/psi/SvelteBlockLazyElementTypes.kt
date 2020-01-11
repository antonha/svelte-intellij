package dev.blachut.svelte.lang.psi

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JavaScriptParser
import dev.blachut.svelte.lang.parsing.html.SvelteManualParsing

object SvelteBlockLazyElementTypes {
    val IF_START = object : SvelteBlockLazyElementType("IF_START") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            builder.advanceLexer() // JSTokenTypes.SHARP
            SvelteManualParsing.parseNotAllowedWhitespace(builder, "#")
            builder.advanceLexer() // JSTokenTypes.IF_KEYWORD

            parser.expressionParser.parseExpression()
        }
    }

    val ELSE_CLAUSE = object : SvelteBlockLazyElementType("ELSE_CLAUSE") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            builder.advanceLexer() // JSTokenTypes.COLON
            SvelteManualParsing.parseNotAllowedWhitespace(builder, ":")
            builder.advanceLexer() // JSTokenTypes.ELSE_KEYWORD

            if (builder.tokenType === JSTokenTypes.IF_KEYWORD) {
                builder.advanceLexer()
                parser.expressionParser.parseExpression()
            }
        }
    }

    val IF_END = SvelteElementType("IF_END")
    val EACH_END = SvelteElementType("EACH_END")
    val AWAIT_END = SvelteElementType("AWAIT_END")

    val EACH_START = object : SvelteBlockLazyElementType("EACH_START") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            builder.advanceLexer() // JSTokenTypes.SHARP
            SvelteManualParsing.parseNotAllowedWhitespace(builder, "#")
            builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake EACH

            parser.expressionParser.parseExpression()

            if (builder.tokenType === JSTokenTypes.AS_KEYWORD) {
                builder.advanceLexer()
            } else {
                builder.error("as expected")
            }

            parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)

            if (builder.tokenType === JSTokenTypes.COMMA) {
                builder.advanceLexer()
                // TODO disallow destructuring
                parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
            }

            if (builder.tokenType === JSTokenTypes.LPAR) {
                builder.advanceLexer()
                parser.expressionParser.parseExpression()

                if (builder.tokenType === JSTokenTypes.RPAR) {
                    builder.advanceLexer()
                } else {
                    builder.error(") expected")
                }
            }

        }
    }

    val AWAIT_START = object : SvelteBlockLazyElementType("AWAIT_START") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            builder.advanceLexer() // JSTokenTypes.SHARP
            SvelteManualParsing.parseNotAllowedWhitespace(builder, "#")
            builder.advanceLexer() // JSTokenTypes.AWAIT_KEYWORD

            parser.expressionParser.parseExpression()

            if (builder.tokenType === JSTokenTypes.IDENTIFIER && builder.tokenText == "then") {
                builder.advanceLexer()

                parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
            }
        }
    }

    val THEN_CLAUSE = object : SvelteBlockLazyElementType("THEN_CLAUSE") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            builder.advanceLexer() // JSTokenTypes.COLON
            SvelteManualParsing.parseNotAllowedWhitespace(builder, ":")
            builder.advanceLexer() // JSTokenTypes.IDENTIFIER -- fake THEN

            // TODO Check weird RBRACE placement
            parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
        }
    }

    val CATCH_CLAUSE = object : SvelteBlockLazyElementType("CATCH_CLAUSE") {
        override val noTokensErrorMessage = "expression expected"

        override fun parseTokens(builder: PsiBuilder, parser: JavaScriptParser<*, *, *, *>) {
            builder.advanceLexer() // JSTokenTypes.COLON
            SvelteManualParsing.parseNotAllowedWhitespace(builder, ":")
            builder.advanceLexer() // JSTokenTypes.CATCH_KEYWORD

            parser.expressionParser.parseDestructuringElement(SvelteJSElementTypes.PARAMETER, false, false)
        }
    }
}