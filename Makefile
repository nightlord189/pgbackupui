run:
	docker-compose up -d

run-test-db:
	docker-compose -f docker-compose-test.yml up -d

stop:
	docker-compose down

stop-test-db:
	docker-compose -f docker-compose-test.yml down