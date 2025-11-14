package com.devhunter.bounty.controller;

import com.devhunter.bounty.model.dto.BountyClaimDTO;
import com.devhunter.bounty.model.dto.BountyCreateDTO;
import com.devhunter.bounty.model.dto.BountySubmissionRequestDTO;
import com.devhunter.bounty.model.entity.Bounty;
import com.devhunter.bounty.service.BountyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bounties")
@RequiredArgsConstructor
public class BountyController {

    private final BountyService bountyService;

    @PostMapping
    public ResponseEntity<Bounty> createBounty(@RequestBody BountyCreateDTO dto) {
        return ResponseEntity.ok(bountyService.createBounty(dto));
    }

    @GetMapping
    public ResponseEntity<List<Bounty>> getOpenBounties() {
        return ResponseEntity.ok(bountyService.getOpenBounties());
    }

    @PutMapping("/{id}/claim")
    public ResponseEntity<Bounty> claimBounty(@PathVariable Long id, @RequestBody BountyClaimDTO dto) {
        return ResponseEntity.ok(bountyService.claimBounty(id, dto.getHunterId()));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submitBounty(@PathVariable Long id, @RequestBody BountySubmissionRequestDTO dto) {
        bountyService.submitBounty(id, dto.getHunterId());
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBounty(@PathVariable Long id) {
        bountyService.deleteBounty(id);
        return ResponseEntity.noContent().build();
    }
}

