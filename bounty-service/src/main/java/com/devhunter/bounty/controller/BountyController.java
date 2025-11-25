package com.devhunter.bounty.controller;

import com.devhunter.bounty.model.dto.BountyClaimDTO;
import com.devhunter.bounty.model.dto.BountyCreateDTO;
import com.devhunter.bounty.model.dto.BountySubmissionRequestDTO;
import com.devhunter.bounty.model.entity.Bounty;
import com.devhunter.bounty.service.BountyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bounties")
@RequiredArgsConstructor
public class BountyController {

    private final BountyService bountyService;

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Bounty> createBounty(@RequestBody BountyCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bountyService.createBounty(dto));
    }

    @GetMapping
    public ResponseEntity<List<Bounty>> getOpenBounties() {
        return ResponseEntity.ok(bountyService.getBounties());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Bounty>> getPendingBounties() {
        return ResponseEntity.ok(bountyService.getPendingBounties());
    }

    @PutMapping("/{id}/claim")
    @PreAuthorize("hasRole('HUNTER')")
    public ResponseEntity<Bounty> claimBounty(@PathVariable Long id, @RequestBody(required = false) BountyClaimDTO dto) {
        Long hunterId = dto != null ? dto.getHunterId() : null;
        return ResponseEntity.accepted().body(bountyService.requestClaim(id, hunterId));
    }

    @PutMapping("/{id}/claim/approve")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Bounty> approveClaim(@PathVariable Long id) {
        return ResponseEntity.ok(bountyService.approveClaim(id));
    }

    @PutMapping("/{id}/claim/reject")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Bounty> rejectClaim(@PathVariable Long id) {
        return ResponseEntity.ok(bountyService.rejectClaim(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> completeBounty(@PathVariable Long id) {
        bountyService.completeBounty(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/review/reject")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> rejectReview(@PathVariable Long id, @RequestBody(required = false) String reason) {
        bountyService.rejectReview(id, reason != null ? reason : "Motivo não informado");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('HUNTER')")
    public ResponseEntity<Void> submitBounty(@PathVariable Long id, @RequestBody(required = false) BountySubmissionRequestDTO dto) {
        Long hunterId = dto != null ? dto.getHunterId() : null;
        bountyService.submitBounty(id, hunterId);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> deleteBounty(@PathVariable Long id) {
        bountyService.deleteBounty(id);
        return ResponseEntity.noContent().build();
    }
}

