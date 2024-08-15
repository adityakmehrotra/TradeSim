<a id="readme-top"></a>

<div align="center">
  
  [![Contributors][contributors-shield]][contributors-url]
  [![Stargazers][stars-shield]][stars-url]
  [![Issues][issues-shield]][issues-url]
  [![MIT License][license-shield]][license-url]
  [![LinkedIn][linkedin-shield]][linkedin-url]
</div>

<br />
<div align="center">
  <a href="https://tradesim.adityakmehrotra">
    <img src="client/public/tradesim_icon.png" alt="TradeSim Logo" width="80" height="80">
  </a>
  
  <h2 align="center">TradeSim</h2>

  <p align="center">
    A web application that lets users simulate stock market investments with paper (virtual) money, track their portfolio performance, and enhance their trading skills without any financial risk. Users have access to comprehensive market data for each company, including charts, news, and detailed financial information. 
    <br />
    <br />
    <a href="https://github.com/adityakmehrotra/TradeSim">View Demo</a>
    ·
    <a href="https://github.com/adityakmehrotra/TradeSim/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
    ·
    <a href="https://github.com/adityakmehrotra/TradeSim/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
  </p>
</div>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#overview">Overview</a></li>
        <li><a href="#features">Features</a></li>
        <li><a href="#deployment">Deployment</a></li>
      </ul>
    </li>
    <li>
      <a href="#built-with">Built With</a>
      <ul>
        <li><a href="#frontend">Frontend</a></li>
        <li><a href="#backend">Backend</a></li>
        <li><a href="#data-visualization">Data Visualization</a></li>
        <li><a href="#deployment-and-devops">Deployment and DevOps</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li>
          <a href="#installation">Installation</a>
          <ul>
            <li><a href="#clone-the-repository">Clone the Repository</a></li>
            <li><a href="#install-backend-dependencies">Install Backend Dependencies</a></li>
            <li><a href="#clean-and-install-backend-dependencies">Clean and Install Frontend Dependencies</a></li>
          </ul>
        </li>
        <li>
          <a href="#installation">Running the Application</a>
          <ul>
            <li><a href="start-the-flask-backend">Start the Flask Backend</a></li>
            <li><a href="start-the-react-fronted">Start the React Frontend</a></li>
          </ul>
        </li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

# About The Project

![TradeSim (1)](https://github.com/user-attachments/assets/9fe749fa-77dc-4cdd-9ff6-5aff45faa185)

[![React][React.js]][React-url]
[![Spring Boot][Spring.io]][Spring-url]
[![MongoDB][MongoDB.com]][MongoDB-url]
[![AWS][AWS.com]][AWS-url]

## Overview
TradeSim is an advanced web application that allows users to simulate stock market trading with paper (virtual) currency. This full-stack application features a React frontend paired with a Spring Boot backend, ensuring a seamless and responsive user experience. The application leverages a MongoDB cluster to efficiently manage and store user data and transaction history. Hosted on an AWS EC2 instance, TradeSim offers a robust platform for practicing market trading risk-free, making it an ideal tool for both novice traders and seasoned investors to hone their skills. Visit the application [here](https://tradesim.adityakmehrotra.com).

## Features
- **Virtual Trading**: Users can simulate buying and selling stocks with virtual currency, practicing market strategies risk-free.
- **Market Data Access**: Access comprehensive market data for companies, including charts, news, and financial information.
- **User Authentication**: Users can create accounts and log in via OAuth to access personalized features.
- **Portfolio Management**: Users can create and manage multiple portfolios, tracking their virtual investments in real-time.
- **Transaction History**: View detailed transaction history, including buy and sell orders, with timestamps and trade specifics.
- **Responsive Design**: Optimized for seamless use across various devices, ensuring a consistent experience on desktops, tablets, and smartphones.
- **Interactive Charts**: Visualize portfolio performance with interactive charts, enabling users to analyze their investment strategies.
- **Real-time Updates**: Stay updated with real-time market data and portfolio value adjustments based on current stock prices.
- **Security**: Ensures data integrity and security with robust backend implementations using Spring Boot.

## Deployment
TradeSim is deployed using various AWS services, including:
- **AWS Elastic Beanstalk (EC2 Instance):** For deploying the Spring backend.
- **AWS Amplify:** For continuous deployment and hosting of the React frontend.
- **AWS Route 53 and Certificate Manager:** For domain name management.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

# Built With

## Frontend
[![React][React.js]][React-url]
[![Bootstrap][Bootstrap.com]][Bootstrap-url]
[![Tailwind CSS][TailwindCSS.com]][TailwindCSS-url]
[![React Hook Form][ReactHookForm.com]][ReactHookForm-url]
[![Figma][Figma.com]][Figma-url]

## Backend
[![Spring Boot][Spring.io]][Spring-url]
[![MongoDB][MongoDB.com]][MongoDB-url]
[![JWT][JWT.io]][JWT-url]
[![OAuth][OAuth.com]][OAuth-url]

## Data Visualization
[![Chart.js][Chartjs.org]][Chartjs-url]

## Deployment and DevOps
[![AWS][AWS.com]][AWS-url]
[![NPM][NPMjs.com]][NPM-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

# Getting Started

## Prerequisites
* npm
  ```sh
  npm install npm@latest -g
  ```

## Installation

### Clone the Repository

```sh
git clone https://github.com/adityakmehrotra/TradeSim.git
cd TradeSim
```

### Clean and Install Backend Dependencies
```sh
# Navigate to the backend server directory
cd backend
mvn clean install
```

### Install Frontend Dependencies
```sh
# Navigate to the frontend directory
cd ../client
npm install
```

## Running the Application

### Start the Spring Boot Backend

```sh
# Navigate to the backend server directory
cd ../server
# Run the backend code to get the server up
```

#### The server should now be running on http://localhost:8000.

### Start the React Frontend

Open another terminal window and navigate to the client directory:
```sh
npm start
```

#### The application should now be running on http://localhost:8001.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

# Usage
1. **Create an Account:** Sign up to create a personal account, enabling access to all of the features.
2. **Log In:** Log in to your account to start trading and managing your portfolio.
3. **Create a Portfolio:** Navigate to the portfolio management section to create and manage multiple portfolios.
4. **Simulate Trades:** Use virtual currency to buy and sell stocks, allowing you to practice trading strategies risk-free.
5. **View Portfolio Performance:** Monitor your portfolio's performance with real-time data and visualizations.
6. **Access Market Data:** Search for companies to view detailed market data, including charts, news, and financial information.
7. **Track Transaction History:** View a comprehensive history of all your trades and transactions within each portfolio.
8. **Manage Account:** Update your account information and manage security settings through the user dashboard.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

# Contributing
Contributions to this project are **greatly appreciated**!

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

# License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

# Contact

For any queries, you can reach out to me at `adi1.mehrotra@gmail.com`.

Project Website: https://tradesim.adityakmehrotra.com

Project Repo: https://github.com/adityakmehrotra/TradeSim

<p align="right">(<a href="#readme-top">back to top</a>)</p>

# Acknowledgments
Thanks to all the Beta testers who helped find bugs in TradeSim.

Special thanks to AWS for hosting services.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://reactjs.org/
[Bootstrap.com]: https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white
[Bootstrap-url]: https://getbootstrap.com/
[TailwindCSS.com]: https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white
[TailwindCSS-url]: https://tailwindcss.com/
[ReactHookForm.com]: https://img.shields.io/badge/React_Hook_Form-EC5990?style=for-the-badge&logo=reacthookform&logoColor=white
[ReactHookForm-url]: https://react-hook-form.com/
[Figma.com]: https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white
[Figma-url]: https://www.figma.com/
[Spring.io]: https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white
[Spring-url]: https://spring.io/projects/spring-boot
[JWT.io]: https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jwt&logoColor=white
[JWT-url]: https://jwt.io/
[OAuth.com]: https://img.shields.io/badge/OAuth-4285F4?style=for-the-badge&logo=oauth&logoColor=white
[OAuth-url]: https://oauth.net/
[Chartjs.org]: https://img.shields.io/badge/Chart.js-F5788D?style=for-the-badge&logo=chartdotjs&logoColor=white
[Chartjs-url]: https://www.chartjs.org/
[MongoDB.com]: https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white
[MongoDB-url]: https://www.mongodb.com/
[AWS.com]: https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white
[AWS-url]: https://aws.amazon.com/
[NPMjs.com]: https://img.shields.io/badge/NPM-CB3837?style=for-the-badge&logo=npm&logoColor=white
[NPM-url]: https://www.npmjs.com/

[contributors-shield]: https://img.shields.io/github/contributors/adityakmehrotra/TradeSim.svg?style=for-the-badge
[contributors-url]: https://github.com/adityakmehrotra/TradeSim/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/adityakmehrotra/TradeSim.svg?style=for-the-badge
[forks-url]: https://github.com/adityakmehrotra/TradeSim/network/members
[stars-shield]: https://img.shields.io/github/stars/adityakmehrotra/TradeSim.svg?style=for-the-badge
[stars-url]: https://github.com/adityakmehrotra/TradeSim/stargazers
[issues-shield]: https://img.shields.io/github/issues/adityakmehrotra/TradeSim.svg?style=for-the-badge
[issues-url]: https://github.com/adityakmehrotra/TradeSim/issues
[license-shield]: https://img.shields.io/github/license/adityakmehrotra/TradeSim.svg?style=for-the-badge
[license-url]: https://github.com/adityakmehrotra/TradeSim/blob/main/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/aditya-mehrotra-
