# Soul and descriptions sprint

Approved scope, 2026-10-09:

1. Replace the Necromancer's tier-two Soul Siphon with Soul Sustenance. A qualifying hero or raised-minion kill while the Phylactery is already full restores 10/20 normal hunger turns, limited to 60/120 per floor. Extra soul charges do not multiply nourishment. Summoned and rewardless replacement enemies do not qualify. Preserve invested ranks and floor allowances through saving and revisiting; no healing or eating-triggered talents.
2. Review Grimhollow's added descriptions. Give players the primary effect, action costs and meaningful progression alongside flavor. Explain talent ranks, spell ranges and important restrictions. Keep exceptional interactions, quest solutions, betrayal and exact reward tables for discovery. Retain ordinary unidentified-item rules.
3. Extend the existing class/content checks, verify long-description presentation, build Windows and Android, package, push and check the unchanged CI gates. No new art, unrelated balance changes or terrain contrast testing.

Each implementation component will be committed separately. Check usage before beginning the description component; deliver the final build at a clean boundary.

Completed locally: Soul Sustenance, balanced descriptions, combined headless Runs=30 failures=0, eight unit tests, Windows/Android builds, and interface/presentation checks in both orientations. The approved follow-up adds three-rank Bone Legion, Cole's staged payment/betrayal and personal poster, wanted-target/urgency repair for seed 3848062306978, and stronger Horror survival/combat. Package and CI delivery will cover both components together.

Follow-up complete locally: all four servant types and legacy ranks, three-step Cole handover and physical poster (including full pack), reported-seed quarry/clock repair, stronger regional Horrors and saved-state migration. Final Bounty/Horror native suites pass both orientations. Version 1.29.15/code 1014 packages and compiled audit pass; release delivery includes all components.
