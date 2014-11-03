#!/bin/bash

cat * | grep intersection | grep invMint | awk '{print $6}'
