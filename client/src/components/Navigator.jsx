import React, { useEffect, useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';

import AppLayout from './AppLayout';
import Portfolio from './pages/Page 1/Portfolio';
import Notifications from './pages/Page 2/Notifications';
import Account from './pages/Page 3/Account';
import SecurityPage from './SecurityPage';

export default function Navigator() {

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<AppLayout />}>
                    <Route index element={<Portfolio />} />
                    <Route path="/portfolio" element={<Portfolio />} />
                    <Route path="/notifications" element={<Notifications />} />
                    <Route path="/account" element={<Account />} />
                    <Route path="/search/:searchQuery" element={<SecurityPage />} />
                    <Route path="*" element={<Portfolio />} />
                </Route>
            </Routes>
        </BrowserRouter>
  )
}