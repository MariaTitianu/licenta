#!/bin/bash
# Installation script for pg_log extension

set -e  # Exit on error

echo "Installing pg_log PostgreSQL extension..."

# Determine PostgreSQL major version
if command -v pg_config > /dev/null; then
    PG_VERSION=$(pg_config --version | grep -oE '[0-9]+' | head -1)
else
    echo "PostgreSQL not found. Please install PostgreSQL first."
    exit 1
fi

echo "Detected PostgreSQL $PG_VERSION"

# Check if development packages are installed
if [ ! -d "$(pg_config --includedir-server)" ]; then
    echo "PostgreSQL development packages not found. Please install them first."
    echo "For Debian/Ubuntu: sudo apt-get install postgresql-server-dev-$PG_VERSION"
    echo "For RedHat/CentOS: sudo yum install postgresql$PG_VERSION-devel"
    exit 1
fi

# Update Makefile with correct PostgreSQL paths
PG_CONFIG_PATH=$(which pg_config)
sed -i "s|PG_CONFIG = .*|PG_CONFIG = $PG_CONFIG_PATH|" Makefile

# Build and install
echo "Building extension..."
make clean || true
make

echo "Installing extension (requires sudo)..."
sudo make install

echo "Installation complete!"
echo
echo "To activate the extension in your database, connect to PostgreSQL and run:"
echo "  CREATE EXTENSION pg_log;"
echo
echo "For more information, see README.md" 