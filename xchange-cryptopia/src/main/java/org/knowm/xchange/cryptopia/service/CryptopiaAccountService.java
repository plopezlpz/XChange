package org.knowm.xchange.cryptopia.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.knowm.xchange.cryptopia.CryptopiaExchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.DefaultWithdrawFundsParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;

public class CryptopiaAccountService extends CryptopiaAccountServiceRaw implements AccountService {
  public CryptopiaAccountService(CryptopiaExchange exchange) {
    super(exchange);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    List<Balance> balances = getBalances();

    return new AccountInfo(new Wallet(balances));
  }

  @Override
  public String withdrawFunds(Currency currency, BigDecimal amount, String address)
      throws IOException {
    return withdrawFunds(new DefaultWithdrawFundsParams(address, currency, amount));
  }

  @Override
  public String withdrawFunds(WithdrawFundsParams params) throws IOException {
    if (params instanceof DefaultWithdrawFundsParams) {
      DefaultWithdrawFundsParams defaultWithdrawFundsParams = (DefaultWithdrawFundsParams) params;
      return submitWithdraw(
          defaultWithdrawFundsParams.getCurrency(),
          defaultWithdrawFundsParams.getAmount(),
          defaultWithdrawFundsParams.getAddress(),
          null);
    } else {
      throw new IllegalStateException("Don't understand " + params);
    }
  }

  @Override
  public String requestDepositAddress(Currency currency, String... args) throws IOException {
    return getDepositAddress(currency);
  }

  @Override
  public TradeHistoryParams createFundingHistoryParams() {
    return new CryptopiaFundingHistoryParams(CryptopiaFundingHistoryParams.Type.Deposit, 100);
  }

  @Override
  public List<FundingRecord> getFundingHistory(TradeHistoryParams params) throws IOException {
    if (params instanceof CryptopiaFundingHistoryParams) {
      CryptopiaFundingHistoryParams cryptopiaFundingHistoryParams =
          (CryptopiaFundingHistoryParams) params;

      return getTransactions(
          cryptopiaFundingHistoryParams.type.name(), cryptopiaFundingHistoryParams.count);
    } else {
      return getTransactions(CryptopiaFundingHistoryParams.Type.Deposit.name(), 100);
    }
  }

  public static class CryptopiaFundingHistoryParams implements TradeHistoryParams {
    public final Type type;
    public final Integer count;

    public CryptopiaFundingHistoryParams(Type type, Integer count) {
      this.type = type;
      this.count = count;
    }

    public enum Type {
      Deposit,
      Withdraw
    }
  }
}
