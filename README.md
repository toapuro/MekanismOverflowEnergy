# Mekanism Overflow Energy

By default, Mekanism is limited by the ForgeCapabilities.ENERGY system,
which caps a single energy transfer at Integer.MAX_VALUE (~2.14G FE).

Allows up to `Integer.MAX_VALUE * maxOperationCount` transfers per tick

# Configuration
A config file is generated at:

config/mekanismoverflowenergy-common.toml

`maxOperationCount` - the maximum number of repeated transfers
(default: 256)
