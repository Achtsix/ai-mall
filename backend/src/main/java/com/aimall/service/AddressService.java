package com.aimall.service;

import com.aimall.common.BusinessException;
import com.aimall.common.UserContext;
import com.aimall.entity.Address;
import com.aimall.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressMapper addressMapper;

    public AddressService(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public List<Address> list() {
        return addressMapper.findByUserId(UserContext.getUserId());
    }

    @Transactional
    public Address add(Address address) {
        address.setUserId(UserContext.getUserId());
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefault(address.getUserId());
        } else {
            address.setIsDefault(0);
        }
        addressMapper.insert(address);
        return address;
    }

    @Transactional
    public Address update(Address address) {
        Long userId = UserContext.getUserId();
        Address exist = addressMapper.findByIdAndUser(address.getId(), userId);
        if (exist == null) {
            throw new BusinessException(404, "地址不存在");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefault(userId);
        }
        address.setUserId(userId);
        addressMapper.update(address);
        return address;
    }

    public void delete(Long id) {
        addressMapper.delete(id, UserContext.getUserId());
    }
}
