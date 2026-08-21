package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.Wallet;
import com.aimall.entity.WalletRecharge;
import com.aimall.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public Result<Wallet> myWallet() {
        return Result.ok(walletService.getMyWallet());
    }

    @PostMapping("/recharge")
    public Result<WalletRecharge> recharge(@RequestBody Map<String, BigDecimal> req) {
        return Result.ok(walletService.recharge(req.get("amount")));
    }

    @GetMapping("/recharges")
    public Result<List<WalletRecharge>> recharges() {
        return Result.ok(walletService.rechargeRecords());
    }
}
