# ============================================
# SUPABASE SEED DATA SCRIPT (PowerShell)
# Usage: .\supabase_seed.ps1
# ============================================

Write-Host "============================================" -ForegroundColor Yellow
Write-Host "  Bookverse - Supabase Seed Data Import" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow
Write-Host ""

# Check if Supabase CLI is installed
$supabaseCli = Get-Command supabase -ErrorAction SilentlyContinue
if (-not $supabaseCli) {
    Write-Host "❌ Supabase CLI is not installed" -ForegroundColor Red
    Write-Host "Install it with: npm install -g supabase" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Supabase CLI found" -ForegroundColor Green

# Check if seed_data.sql exists
if (-not (Test-Path "seed_data.sql")) {
    Write-Host "❌ seed_data.sql not found" -ForegroundColor Red
    exit 1
}

Write-Host "✓ seed_data.sql found" -ForegroundColor Green
Write-Host ""

# Prompt for Database URL
Write-Host "Enter your Supabase Database URL:" -ForegroundColor Yellow
Write-Host "(Format: postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres)" -ForegroundColor Cyan
$databaseUrl = Read-Host "Database URL"

if ([string]::IsNullOrWhiteSpace($databaseUrl)) {
    Write-Host "❌ Database URL is required" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Executing seed data..." -ForegroundColor Yellow

# Execute using psql (if available)
$psql = Get-Command psql -ErrorAction SilentlyContinue
if ($psql) {
    psql $databaseUrl -f seed_data.sql
} else {
    # Alternative: Use Supabase CLI
    Write-Host "psql not found. Using alternative method..." -ForegroundColor Yellow
    Get-Content seed_data.sql | supabase db execute --db-url $databaseUrl
}

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "✓ Seed data imported successfully!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "❌ Error importing seed data" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    exit 1
}
