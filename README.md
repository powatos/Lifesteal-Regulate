# Lifesteal Regulate
by powato

## Features

* Lifesteal
  * Steal a player's heart by killing them
  * Players with 1 heart will not get it stolen when killed

* Safe chunks
  * Add chunks to a safe list that disables PvP
  

* Command Index: `/lifestealR AddChunk|RemoveChunk|SafechunkVisibility <true|false>|ResetChunks`

## Known issues

* Inconsistent culling in safe chunks debug rendering mode
* Inconsistent transparency effects in safe chunks debug rendering mode
  * fix: stack colors? through multiple safe chunk renders
* Inconsistent projectile visibility when deflected in safe chunk