provider "github" {
  token        = "${var.GITHUB_TOKEN}"
  organization = "${var.GITHUB_ORGANIZATION}"
}

resource "github_repository" "terraform_test" {
  name = "terraform_test"
}
