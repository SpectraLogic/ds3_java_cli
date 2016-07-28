require "faraday"
require "json"

ENV["DS3_ENDPOINT"] = ENV["DS3_ENDPOINT"] || "sm2u-11"

# Has to be to https and not http.
connection = Faraday.new(:url =>  "https://#{ENV["DS3_ENDPOINT"]}-mgmt.eng.sldomain.com",
                                            # Don't worry about verifying the SSL certificate.
                                            :ssl => { :verify => false }) do |conn|
  conn.request(:basic_auth, "spectra", "spectra")
  # User Ruby's net/http library
  conn.adapter(:net_http)
  # Setup for JSON requests and responses.
  conn.headers = { "Accept" => "application/json",
                   "Content-Type" => "application/json" }
end

# List all users
response = connection.get("/api/users")
# All GET responses are under a root "data"
users = JSON.parse(response.body)["data"]

spectra_user = {}
users.each do | user_entry |
  spectra_user = user_entry if user_entry["name"].eql?("Spectra")
end

# None of the "/api/users" responses include the S3 keys.  For that, you have to
# make a separate "/api/ds3/keys" request which is setup to return an array
# of DS3 authid/secretkey pairs in case we ever allow more than one pair per
# user.
response = connection.get("/api/ds3/keys?user_id=#{spectra_user["id"]}")
spectra_user_keys = JSON.parse(response.body)["data"][0]
abort("Spectra User Keys not found.") if spectra_user.keys.nil?

ENV["DS3_ENDPOINT"] = "#{ENV['DS3_ENDPOINT']}.eng.sldomain.com"
ENV["DS3_SECRET_KEY"] = spectra_user_keys["secret_key"]
ENV["DS3_ACCESS_KEY"] = spectra_user_keys["auth_id"]
puts "DS3_ENDPOINT #{ENV['DS3_ENDPOINT']}"
puts "DS3_SECRET_KEY #{ENV["DS3_SECRET_KEY"]}"
puts "DS3_ACCESS_KEY #{ENV["DS3_ACCESS_KEY"]}"

ENV["GIT_REPO"] = ENV["GIT_REPO"] || "https://github.com/SpectraLogic/ds3_java_cli.git"
ENV["GIT_BRANCH"] = ENV["GIT_BRANCH"] || "master"
puts "GIT_REPO #{ENV["GIT_REPO"]}"
puts "GIT_BRANCH #{ENV["GIT_BRANCH"]}"

ENV["DOCKER_REPO"] = ENV["DOCKER_REPO"] || "denverm80/ds3_jsdk_docker_test:latest"
puts "DOCKER_REPO #{ENV["DOCKER_REPO"]}"

# pull down the git repo
# puts `git clone #{ENV["GIT_REPO"]} --branch #{ENV["GIT_BRANCH"]} --single-branch`
#
# # Build latest docker image
# puts "docker build -t #{ENV['DOCKER_REPO']} ./ds3_java_cli/docker/"
# docker_build_output = `docker build -t #{ENV["DOCKER_REPO"]} ./ds3_java_cli/docker/`
# docker_build_status = $?
# puts docker_build_output
# puts "docker build status[#{docker_build_status}][#{docker_build_status.exitstatus}]"
#
puts "docker run -e DS3_ENDPOINT -e DS3_SECRET_KEY -e DS3_ACCESS_KEY -e GIT_REPO -e GIT_BRANCH --dns=10.1.0.9 #{ENV["DOCKER_REPO"]}"
output = `docker run -e DS3_ENDPOINT -e DS3_SECRET_KEY -e DS3_ACCESS_KEY -e GIT_REPO -e GIT_BRANCH --dns=10.1.0.9 #{ENV["DOCKER_REPO"]}`
docker_status = $?

puts output
puts "docker status[#{docker_status}][#{docker_status.exitstatus}]"

# clean up git repo
puts `rm -rf ds3_java_cli`

exit docker_status.exitstatus

