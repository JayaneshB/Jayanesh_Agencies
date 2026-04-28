package com.chocowholesale.backend.web;

import com.chocowholesale.backend.dto.AddressDto;
import com.chocowholesale.backend.dto.AddressRequest;
import com.chocowholesale.backend.entity.Address;
import com.chocowholesale.backend.entity.User;
import com.chocowholesale.backend.repository.AddressRepository;
import com.chocowholesale.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressRepository addressRepo;
    private final UserRepository userRepo;

    public AddressController(AddressRepository addressRepo, UserRepository userRepo) {
        this.addressRepo = addressRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<List<AddressDto>> list(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(
                addressRepo.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                        .stream().map(AddressDto::from).toList());
    }

    @PostMapping
    public ResponseEntity<AddressDto> create(
            @AuthenticationPrincipal UUID userId, @RequestBody AddressRequest req) {
        User user = userRepo.findById(userId).orElseThrow();
        Address a = new Address();
        a.setUser(user);
        applyFields(a, req);
        if (Boolean.TRUE.equals(req.isDefault())) {
            clearDefaults(userId);
        }
        return ResponseEntity.ok(AddressDto.from(addressRepo.save(a)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> update(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id, @RequestBody AddressRequest req) {
        Address a = addressRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!a.getUser().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        applyFields(a, req);
        if (Boolean.TRUE.equals(req.isDefault())) {
            clearDefaults(userId);
        }
        return ResponseEntity.ok(AddressDto.from(addressRepo.save(a)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        Address a = addressRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!a.getUser().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        addressRepo.delete(a);
        return ResponseEntity.noContent().build();
    }

    private void applyFields(Address a, AddressRequest req) {
        a.setLine1(req.line1());
        a.setLine2(req.line2());
        a.setCity(req.city());
        a.setState(req.state());
        a.setPincode(req.pincode());
        if (req.country() != null) a.setCountry(req.country());
        a.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
    }

    private void clearDefaults(UUID userId) {
        addressRepo.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream().filter(Address::getIsDefault)
                .forEach(addr -> { addr.setIsDefault(false); addressRepo.save(addr); });
    }
}
