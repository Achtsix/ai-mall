package com.aimall.service;

import com.aimall.common.BusinessException;
import com.aimall.common.UserContext;
import com.aimall.entity.Wallet;
import com.aimall.entity.WalletRecharge;
import com.aimall.mapper.WalletMapper;
import com.aimall.mapper.WalletRechargeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletMapper walletMapper;
    private final WalletRechargeMapper rechargeMapper;

    public WalletService(WalletMapper walletMapper, WalletRechargeMapper rechargeMapper) {
        this.walletMapper = walletMapper;
        this.rechargeMapper = rechargeMapper;
    }

    public Wallet getMyWallet() {
        Long userId = UserContext.getUserId();
        Wallet wallet = walletMapper.findByUserId(userId);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            walletMapper.insert(wallet);
        }
        return wallet;
    }

    @Transactional
    public WalletRecharge recharge(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "充值金额必须大于0");
        }
        Long userId = UserContext.getUserId();
        Wallet wallet = getMyWallet();
        walletMapper.increase(userId, amount);
        wallet.setBalance(wallet.getBalance().add(amount));
        WalletRecharge recharge = new WalletRecharge();
        recharge.setUserId(userId);
        recharge.setAmount(amount);
        recharge.setBalanceAfter(wallet.getBalance());
        recharge.setStatus(1);
        rechargeMapper.insert(recharge);
        return recharge;
    }

    public List<WalletRecharge> rechargeRecords() {
        return rechargeMapper.findByUserId(UserContext.getUserId());
    }

    @Transactional
    public boolean pay(Long userId, BigDecimal amount) {
        int rows = walletMapper.decrease(userId, amount);
        if (rows <= 0) {
            throw new BusinessException(400, "余额不足");
        }
        return true;
    }
}
