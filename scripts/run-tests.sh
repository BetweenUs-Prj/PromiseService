#!/bin/bash

# PromiseService 테스트 실행 스크립트
# 이유: 다양한 테스트 시나리오를 체계적으로 실행하여 전체 시스템의 안정성을 검증하기 위해

echo "🧪 PromiseService 테스트 시작..."
echo "=================================="

# 1. 단위 테스트 실행
echo "📋 1. 단위 테스트 실행 중..."
./gradlew test --tests "*.service.*Test" --info

if [ $? -eq 0 ]; then
    echo "✅ 단위 테스트 통과"
else
    echo "❌ 단위 테스트 실패"
    exit 1
fi

# 2. 통합 테스트 실행
echo ""
echo "📋 2. 통합 테스트 실행 중..."
./gradlew test --tests "*ControllerTest" --info

if [ $? -eq 0 ]; then
    echo "✅ 통합 테스트 통과"
else
    echo "❌ 통합 테스트 실패"
    exit 1
fi

# 3. 리포지토리 테스트 실행 (Testcontainers)
echo ""
echo "📋 3. 리포지토리 테스트 실행 중..."
./gradlew test --tests "*RepositoryTest" --info

if [ $? -eq 0 ]; then
    echo "✅ 리포지토리 테스트 통과"
else
    echo "❌ 리포지토리 테스트 실패"
    exit 1
fi

# 4. 전체 테스트 실행
echo ""
echo "📋 4. 전체 테스트 실행 중..."
./gradlew test --info

if [ $? -eq 0 ]; then
    echo "✅ 모든 테스트 통과!"
    echo ""
    echo "🎉 테스트 완료 - 모든 테스트가 성공했습니다!"
else
    echo "❌ 일부 테스트 실패"
    exit 1
fi

# 5. 테스트 커버리지 리포트 생성 (옵션)
echo ""
echo "📊 테스트 커버리지 리포트 생성 중..."
./gradlew jacocoTestReport

echo ""
echo "📁 테스트 결과:"
echo "- 테스트 리포트: build/reports/tests/test/index.html"
echo "- 커버리지 리포트: build/reports/jacoco/test/html/index.html"
echo ""
echo "🚀 테스트 실행 완료!"









