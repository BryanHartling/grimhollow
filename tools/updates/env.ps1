# SPDX-License-Identifier: GPL-3.0-or-later
# Dot-source before building a locally signed Android release. Secrets stay process-local.
$updateKeyDirectory = Join-Path (Split-Path -Parent (Split-Path -Parent $PSScriptRoot)) '.local/update-signing'
$updatePassword = ConvertTo-SecureString (Get-Content -LiteralPath (Join-Path $updateKeyDirectory 'android-store-password.dpapi') -Raw).Trim()
$env:GRIMHOLLOW_ANDROID_KEYSTORE = Join-Path $updateKeyDirectory 'android-release.p12'
$env:GRIMHOLLOW_ANDROID_STORE_PASSWORD = [pscredential]::new('signing', $updatePassword).GetNetworkCredential().Password
$env:GRIMHOLLOW_ANDROID_KEY_PASSWORD = $env:GRIMHOLLOW_ANDROID_STORE_PASSWORD
