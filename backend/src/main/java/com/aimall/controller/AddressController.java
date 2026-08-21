package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.Address;
import com.aimall.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public Result<List<Address>> list() {
        return Result.ok(addressService.list());
    }

    @PostMapping
    public Result<Address> add(@RequestBody Address address) {
        return Result.ok(addressService.add(address));
    }

    @PutMapping
    public Result<Address> update(@RequestBody Address address) {
        return Result.ok(addressService.update(address));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.ok();
    }
}
