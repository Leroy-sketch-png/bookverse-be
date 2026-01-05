#!/bin/bash
# ============================================
# SUPABASE SEED DATA SCRIPT
# Usage: ./supabase_seed.sh
# ============================================

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}  Bookverse - Supabase Seed Data Import${NC}"
echo -e "${YELLOW}============================================${NC}"
echo ""

# Check if Supabase CLI is installed
if ! command -v supabase &> /dev/null
then
    echo -e "${RED}❌ Supabase CLI is not installed${NC}"
    echo -e "${YELLOW}Install it with: npm install -g supabase${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Supabase CLI found${NC}"

# Check if seed_data.sql exists
if [ ! -f "seed_data.sql" ]; then
    echo -e "${RED}❌ seed_data.sql not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ seed_data.sql found${NC}"
echo ""

# Link to your Supabase project (if not already linked)
echo -e "${YELLOW}Linking to Supabase project...${NC}"
# Uncomment and replace with your project reference
# supabase link --project-ref YOUR_PROJECT_REF

# Execute the seed data
echo -e "${YELLOW}Executing seed data...${NC}"
supabase db reset --db-url $DATABASE_URL < seed_data.sql

# Alternative: Push to remote database
# psql $DATABASE_URL < seed_data.sql

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}✓ Seed data imported successfully!${NC}"
    echo -e "${GREEN}============================================${NC}"
else
    echo ""
    echo -e "${RED}============================================${NC}"
    echo -e "${RED}❌ Error importing seed data${NC}"
    echo -e "${RED}============================================${NC}"
    exit 1
fi
