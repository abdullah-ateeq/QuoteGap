package com.example.quotegap

import com.example.quotegap.data.model.AppSettings
import com.example.quotegap.data.model.ComparisonMode
import com.example.quotegap.data.model.QuoteInput
import com.example.quotegap.data.model.QuoteStatus
import com.example.quotegap.data.model.SavedCalculation
import com.example.quotegap.domain.MoneyFormatter
import com.example.quotegap.domain.QuoteGapCalculator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class QuoteGapCalculatorTest {

    // 1. Under-budget quote
    @Test
    fun testUnderBudgetQuote() {
        val input = QuoteInput(
            targetBudget = BigDecimal("100000"),
            quotedPrice = BigDecimal("92000"),
            mode = ComparisonMode.TOTAL
        )
        val result = QuoteGapCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("-8000").compareTo(result.signedDifference))
        assertEquals(0, BigDecimal("8000").compareTo(result.absoluteDifference))
        assertEquals(0, BigDecimal("8").compareTo(result.percentageDifference.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("92").compareTo(result.budgetUsagePercent.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(QuoteStatus.UNDER_BUDGET, result.status)
    }

    // 2. Exact-budget quote
    @Test
    fun testExactBudgetQuote() {
        val input = QuoteInput(
            targetBudget = BigDecimal("100000"),
            quotedPrice = BigDecimal("100000"),
            mode = ComparisonMode.TOTAL
        )
        val result = QuoteGapCalculator.calculateResult(input)

        assertEquals(0, BigDecimal.ZERO.compareTo(result.signedDifference))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.absoluteDifference))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.percentageDifference))
        assertEquals(0, BigDecimal("100").compareTo(result.budgetUsagePercent.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(QuoteStatus.ON_BUDGET, result.status)
    }

    // 3. Over-budget quote
    @Test
    fun testOverBudgetQuote() {
        val input = QuoteInput(
            targetBudget = BigDecimal("100000"),
            quotedPrice = BigDecimal("115000"),
            mode = ComparisonMode.TOTAL
        )
        val result = QuoteGapCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("15000").compareTo(result.signedDifference))
        assertEquals(0, BigDecimal("15000").compareTo(result.absoluteDifference))
        assertEquals(0, BigDecimal("15").compareTo(result.percentageDifference.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("115").compareTo(result.budgetUsagePercent.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(QuoteStatus.OVER_BUDGET, result.status)
    }

    // 4. Zero quoted price
    @Test
    fun testZeroQuotedPrice() {
        val input = QuoteInput(
            targetBudget = BigDecimal("100000"),
            quotedPrice = BigDecimal.ZERO,
            mode = ComparisonMode.TOTAL
        )
        val result = QuoteGapCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("-100000").compareTo(result.signedDifference))
        assertEquals(0, BigDecimal("100000").compareTo(result.absoluteDifference))
        assertEquals(0, BigDecimal("100").compareTo(result.percentageDifference.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal.ZERO.compareTo(result.budgetUsagePercent))
        assertEquals(QuoteStatus.UNDER_BUDGET, result.status)
    }

    // 5. Percentage difference (always against targetBudget)
    @Test
    fun testPercentageDifference() {
        val budget = BigDecimal("100000")
        val quote1 = BigDecimal("95000")
        val quote2 = BigDecimal("125000")

        val res1 = QuoteGapCalculator.calculateResult(QuoteInput(targetBudget = budget, quotedPrice = quote1))
        val res2 = QuoteGapCalculator.calculateResult(QuoteInput(targetBudget = budget, quotedPrice = quote2))

        assertEquals(0, BigDecimal("5").compareTo(res1.percentageDifference.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("25").compareTo(res2.percentageDifference.setScale(0, RoundingMode.HALF_UP)))
    }

    // 6. Budget usage
    @Test
    fun testBudgetUsage() {
        val budget = BigDecimal("100000")
        val quote = BigDecimal("92000")
        val usage = QuoteGapCalculator.calculateBudgetUsage(quote, budget)
        assertEquals(0, BigDecimal("92").compareTo(usage.setScale(0, RoundingMode.HALF_UP)))

        val quoteOver = BigDecimal("115000")
        val usageOver = QuoteGapCalculator.calculateBudgetUsage(quoteOver, budget)
        assertEquals(0, BigDecimal("115").compareTo(usageOver.setScale(0, RoundingMode.HALF_UP)))
    }

    // 7. Large monetary values
    @Test
    fun testLargeMonetaryValues() {
        val budget = BigDecimal("1000000000000.00") // 1 trillion
        val quote = BigDecimal("950000000000.00")
        val input = QuoteInput(targetBudget = budget, quotedPrice = quote)
        val result = QuoteGapCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("-50000000000.00").compareTo(result.signedDifference))
        assertEquals(0, BigDecimal("5").compareTo(result.percentageDifference.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(QuoteStatus.UNDER_BUDGET, result.status)
    }

    // 8. Decimal monetary values
    @Test
    fun testDecimalMonetaryValues() {
        val budget = BigDecimal("1000.50")
        val quote = BigDecimal("975.25")
        val input = QuoteInput(targetBudget = budget, quotedPrice = quote)
        val result = QuoteGapCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("-25.25").compareTo(result.signedDifference))
        assertEquals(0, BigDecimal("25.25").compareTo(result.absoluteDifference))
        assertEquals(QuoteStatus.UNDER_BUDGET, result.status)
    }

    // 9. Total mode
    @Test
    fun testTotalMode() {
        val quote = BigDecimal("75000")
        val total = QuoteGapCalculator.calculateTotalQuote(quote)
        assertEquals(0, quote.compareTo(total))
    }

    // 10. Per-unit mode
    @Test
    fun testPerUnitMode() {
        val budget = BigDecimal("50000")
        val unitPrice = BigDecimal("1200")
        val quantity = 40
        val input = QuoteInput(
            targetBudget = budget,
            unitPrice = unitPrice,
            quantity = quantity,
            mode = ComparisonMode.PER_UNIT
        )
        val result = QuoteGapCalculator.calculateResult(input)

        assertEquals(0, BigDecimal("48000").compareTo(result.quotedTotal))
        assertEquals(0, BigDecimal("-2000").compareTo(result.signedDifference))
        assertEquals(0, BigDecimal("4").compareTo(result.percentageDifference.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("96").compareTo(result.budgetUsagePercent.setScale(0, RoundingMode.HALF_UP)))
        assertEquals(QuoteStatus.UNDER_BUDGET, result.status)
    }

    // 11. Quantity calculation
    @Test
    fun testQuantityCalculation() {
        val unitPrice = BigDecimal("250.75")
        val quantity = 1000
        val total = QuoteGapCalculator.calculateQuotedTotal(unitPrice, quantity)
        assertEquals(0, BigDecimal("250750.00").compareTo(total))
    }

    // 12. Zero quantity rejection
    @Test
    fun testZeroQuantityRejection() {
        val errorZero = QuoteGapCalculator.validateQuantity("0")
        assertNotNull(errorZero)
        assertEquals("Quantity must be a whole number from 1 to 1,000,000.", errorZero)

        val errorNeg = QuoteGapCalculator.validateQuantity("-5")
        assertNotNull(errorNeg)

        val errorTooHigh = QuoteGapCalculator.validateQuantity("1000001")
        assertNotNull(errorTooHigh)

        val valid = QuoteGapCalculator.validateQuantity("40")
        assertNull(valid)
    }

    // 13. Negative budget rejection
    @Test
    fun testNegativeBudgetRejection() {
        val errorNeg = QuoteGapCalculator.validateBudget("-100")
        assertEquals("Budget must be greater than zero.", errorNeg)

        val errorZero = QuoteGapCalculator.validateBudget("0")
        assertEquals("Budget must be greater than zero.", errorZero)

        val valid = QuoteGapCalculator.validateBudget("100000")
        assertNull(valid)
    }

    // 14. Negative quote rejection
    @Test
    fun testNegativeQuoteRejection() {
        val errorNeg = QuoteGapCalculator.validateQuotedPrice("-50")
        assertEquals("Quoted price cannot be negative.", errorNeg)

        val validZero = QuoteGapCalculator.validateQuotedPrice("0")
        assertNull(validZero)

        val validPos = QuoteGapCalculator.validateQuotedPrice("92000")
        assertNull(validPos)
    }

    // 15. Currency persistence and model
    @Test
    fun testCurrencyPersistenceModel() {
        val settingsPkr = AppSettings(defaultCurrency = "PKR")
        assertEquals("PKR", settingsPkr.activeCurrency)

        val settingsOther = AppSettings(defaultCurrency = "Other", customCurrency = "CAD")
        assertEquals("CAD", settingsOther.activeCurrency)

        val formatted = MoneyFormatter.formatMoney(BigDecimal("100000"), "PKR", 2)
        assertEquals("PKR 100,000", formatted)
    }

    // 16. History serialization
    @Test
    fun testHistorySerialization() {
        val list = listOf(
            SavedCalculation(
                projectName = "Kitchen Renovation",
                targetBudget = "100000",
                quotedPrice = "92000",
                quotedTotal = "92000",
                signedDifference = "-8000",
                absoluteDifference = "8000",
                percentageDifference = "8",
                budgetUsagePercent = "92",
                status = QuoteStatus.UNDER_BUDGET
            )
        )

        val gson = Gson()
        val json = gson.toJson(list)
        assertTrue(json.contains("Kitchen Renovation"))

        val type = object : TypeToken<List<SavedCalculation>>() {}.type
        val deserialized: List<SavedCalculation> = gson.fromJson(json, type)
        assertEquals(1, deserialized.size)
        assertEquals("Kitchen Renovation", deserialized[0].projectName)
        assertEquals(QuoteStatus.UNDER_BUDGET, deserialized[0].status)
    }

    // 17. Settings persistence model
    @Test
    fun testSettingsPersistenceModel() {
        val settings = AppSettings()
        assertEquals(2, settings.decimalPlaces)
        assertEquals("PKR", settings.defaultCurrency)
        assertEquals(ComparisonMode.TOTAL, settings.defaultMode)
        assertTrue(settings.showBudgetBar)
        assertTrue(settings.showPercentage)
        assertTrue(settings.showFormula)
    }

    // 18. Favorites sorting
    @Test
    fun testFavoritesSorting() {
        val item1 = SavedCalculation(id = "1", timestamp = 1000L, projectName = "Item 1", isFavorite = false)
        val item2 = SavedCalculation(id = "2", timestamp = 2000L, projectName = "Item 2", isFavorite = true)
        val item3 = SavedCalculation(id = "3", timestamp = 3000L, projectName = "Item 3", isFavorite = false)

        val sorted = listOf(item1, item2, item3).sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )

        assertEquals("2", sorted[0].id) // Favorite first
        assertEquals("3", sorted[1].id) // Newer timestamp next
        assertEquals("1", sorted[2].id) // Oldest last
    }

    // 19. BigDecimal precision
    @Test
    fun testBigDecimalPrecision() {
        val b1 = BigDecimal("0.1")
        val b2 = BigDecimal("0.2")
        val sum = b1.add(b2)
        assertEquals(BigDecimal("0.3"), sum)

        // Decimal formatting
        val money = MoneyFormatter.formatNumber(BigDecimal("100000.5555"), 2)
        assertEquals("100,000.56", money)
    }

    // 20. Status comparison using BigDecimal
    @Test
    fun testStatusComparisonUsingBigDecimal() {
        // Even 0.01 over is OVER_BUDGET
        val over = QuoteGapCalculator.determineStatus(BigDecimal("0.01"))
        assertEquals(QuoteStatus.OVER_BUDGET, over)

        // Even 0.01 under is UNDER_BUDGET
        val under = QuoteGapCalculator.determineStatus(BigDecimal("-0.01"))
        assertEquals(QuoteStatus.UNDER_BUDGET, under)

        // Exact 0 is ON_BUDGET
        val exact = QuoteGapCalculator.determineStatus(BigDecimal.ZERO)
        assertEquals(QuoteStatus.ON_BUDGET, exact)
    }
}
