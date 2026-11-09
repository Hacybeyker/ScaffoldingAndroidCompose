#!/bin/bash
# Instala el pre-commit hook de calidad de código.
# Ejecutar una sola vez tras clonar el repositorio:
#   chmod +x scripts/setup-quality-hook.sh && ./scripts/setup-quality-hook.sh

set -e

HOOK_DIR="$(git rev-parse --git-dir)/hooks"
HOOK_FILE="$HOOK_DIR/pre-commit"

cat > "$HOOK_FILE" <<'EOF'
#!/bin/bash

# Solo se re-stagean los archivos que YA formaban parte de este commit Y que formatAndAnalyze cambió:
# `git add` sobre todo lo modificado arrastraría al commit el trabajo en curso que la persona dejó
# fuera a propósito (git add -p, un commit parcial).
STAGED_KOTLIN=()
while IFS= read -r -d '' file; do
    STAGED_KOTLIN+=("$file")
done < <(git diff --cached --name-only -z --diff-filter=ACMR -- '*.kt' '*.kts')

# formatAndAnalyze analiza el árbol de trabajo, no el índice, y el hook re-stagea el archivo entero. Si un
# archivo tiene cambios en stage Y fuera de stage, el commit se llevaría la parte que la persona dejó fuera
# a propósito, así que se aborta antes de correr Gradle.
if [ ${#STAGED_KOTLIN[@]} -gt 0 ]; then
    PARTIAL=()
    while IFS= read -r -d '' file; do
        PARTIAL+=("$file")
    done < <(GIT_LITERAL_PATHSPECS=1 git diff --name-only -z -- "${STAGED_KOTLIN[@]}")
    if [ ${#PARTIAL[@]} -gt 0 ]; then
        echo "❌ Commit bloqueado — hay archivos Kotlin con cambios en stage y fuera de stage:"
        printf '     %s\n' "${PARTIAL[@]}"
        echo "   Los gates analizan el árbol de trabajo, no el índice, y el hook re-stagea el archivo entero,"
        echo "   así que el commit se llevaría también lo que dejaste fuera."
        echo "   Haz stage del archivo completo (git add <archivo>) o separa el resto con"
        echo "   'git stash push --keep-index', commitea y recupéralo con 'git stash pop'."
        exit 1
    fi
fi

BEFORE=()
for file in "${STAGED_KOTLIN[@]}"; do
    BEFORE+=("$(git hash-object -- "$file")")
done

./gradlew formatAndAnalyze --quiet
FORMAT_EXIT=$?

CHANGED=()
for i in "${!STAGED_KOTLIN[@]}"; do
    if [ "$(git hash-object -- "${STAGED_KOTLIN[$i]}")" != "${BEFORE[$i]}" ]; then
        CHANGED+=("${STAGED_KOTLIN[$i]}")
    fi
done

if [ ${#CHANGED[@]} -gt 0 ]; then
    git add -- "${CHANGED[@]}"
    echo "ℹ️  formatAndAnalyze corrigió el formato de ${#CHANGED[@]} archivo(s) y el commit los incluye:"
    printf '     %s\n' "${CHANGED[@]}"
fi

if [ $FORMAT_EXIT -ne 0 ]; then
    echo "❌ Commit bloqueado — hay errores que no se pueden auto-corregir. Revisa la salida anterior."
    exit 1
fi

exit 0
EOF

chmod +x "$HOOK_FILE"
echo "✅ pre-commit hook instalado en $HOOK_FILE"
