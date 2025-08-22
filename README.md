# Mekanism Overflow Energy

By default, Mekanism is limited by the ForgeCapabilities.ENERGY system,
which caps a single energy transfer at Integer.MAX_VALUE (~2.14G FE).

Mekanism Overflow Energy removes this bottleneck by sending
Integer.MAX_VALUE multiple times per transfer, allowing you to
transmit virtually unlimited amounts of energy in a single tick.

# Configuration
A config file is generated at:

config/mekanismoverflowenergy-common.toml

`maxOperationCount` - the maximum number of repeated transfers
(default: 256)

This means the effective maximum transfer per tick is:

`Integer.MAX_VALUE * maxOperationCount`
