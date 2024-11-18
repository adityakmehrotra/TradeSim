import React, { useEffect, useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';

import AppLayout from './AppLayout';
import Home from './pages/Page 0/Home'
import Portfolio from './pages/Page 1/PortfolioList';
import PortfolioDetails from "./pages/Page 1/PortfolioDetails";
import Notifications from './pages/Page 2/Notifications';
import Account from './pages/Page 3/Account';
import SecurityPage from './SecurityPage';

export default function Navigator({ setActiveTab }) {

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<AppLayout />}>
                    <Route index element={<Home setActiveTab={setActiveTab} />} />
                    <Route path="/portfolio" element={<Portfolio setActiveTab={setActiveTab} />} />
                    <Route path="/portfolio/:portfolioID" element={<PortfolioDetails />} />
                    {/* <Route path="/notifications" element={<Notifications />} /> */}
                    <Route path="/account" element={<Account setActiveTab={setActiveTab} />} />
                    <Route path="/search/:searchQuery" element={<SecurityPage setActiveTab={setActiveTab} />} />
                    <Route path="*" element={<Portfolio />} />
                </Route>
            </Routes>
        </BrowserRouter>
  )
}