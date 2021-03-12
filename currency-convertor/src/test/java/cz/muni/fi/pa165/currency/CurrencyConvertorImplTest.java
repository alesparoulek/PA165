package cz.muni.fi.pa165.currency;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.*;


public class CurrencyConvertorImplTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency CZK = Currency.getInstance("CZK");

    @Mock
    ExchangeRateTable exchangeRateTable;

    CurrencyConvertor currencyConvertor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        currencyConvertor = new CurrencyConvertorImpl(exchangeRateTable);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testConvert() throws ExternalServiceFailureException {

        when(exchangeRateTable.getExchangeRate(EUR, CZK))
                .thenReturn(new BigDecimal("0.1"));

        assertEquals(new BigDecimal("1.00"), currencyConvertor.convert(EUR, CZK, new BigDecimal("10.049999")));
        assertEquals(new BigDecimal("1.01"), currencyConvertor.convert(EUR, CZK, new BigDecimal("10.10999")));
        assertEquals(new BigDecimal("1.02"), currencyConvertor.convert(EUR, CZK, new BigDecimal("10.1999")));

        when(exchangeRateTable.getExchangeRate(CZK, EUR))
                .thenReturn(new BigDecimal("0.038"));

        assertEquals(new BigDecimal("0.04"), currencyConvertor.convert(CZK, EUR, new BigDecimal("1")));
        assertEquals(new BigDecimal("0.04"), currencyConvertor.convert(CZK, EUR, new BigDecimal("1.02")));
        assertEquals(new BigDecimal("0.08"), currencyConvertor.convert(CZK, EUR, new BigDecimal("2.00000000035")));
        assertEquals(new BigDecimal("3800000000000.00"), currencyConvertor.convert(CZK, EUR, new BigDecimal("100000000000000")));
        assertEquals(new BigDecimal("-0.19"), currencyConvertor.convert(CZK, EUR, new BigDecimal("-5")));
        assertEquals(new BigDecimal("-0.50"), currencyConvertor.convert(CZK, EUR, new BigDecimal("-13.123456789")));
    }

    @Test
    public void testConvertWithNullSourceCurrency() {
        expectedException.expect(IllegalArgumentException.class);
        currencyConvertor.convert(null, CZK, BigDecimal.ONE);
    }

    @Test
    public void testConvertWithNullTargetCurrency() {
        expectedException.expect(IllegalArgumentException.class);
        currencyConvertor.convert(EUR, null, BigDecimal.ONE);
    }

    @Test
    public void testConvertWithNullSourceAmount() {
        expectedException.expect(IllegalArgumentException.class);
        currencyConvertor.convert(EUR, CZK, null);
    }

    @Test
    public void testConvertWithUnknownCurrency() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(EUR, CZK))
                .thenReturn(null);
        expectedException.expect(UnknownExchangeRateException.class);
        currencyConvertor.convert(EUR, CZK, BigDecimal.ONE);
    }

    @Test
    public void testConvertWithExternalServiceFailure() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(EUR, CZK))
                .thenThrow(ExternalServiceFailureException.class);
        expectedException.expect(UnknownExchangeRateException.class);
        currencyConvertor.convert(EUR, CZK, BigDecimal.ONE);
    }

}
